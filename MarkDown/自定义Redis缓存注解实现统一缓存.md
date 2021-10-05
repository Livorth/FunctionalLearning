## 自定义Redis缓存注解实现统一缓存

### Spring Cache

这里推荐看 [统一缓存帝国，实战 Spring Cache！](https://xie.infoq.cn/article/26c54c246279306df0c291327)，不能说感受良多，只能说受益匪浅

但是讲Spring Cache不是我的本意，所以Spring Cache的部分就不过多介绍，只简单描述使用过程

1. 添加依赖：spring-boot-starter-cache
2. 配置缓存类型，我推荐用caffeine或者redis，后面以redis为例
3. 在主启动类或者任意配置类上加上`@EnableCaching`注解
4. 最后在指定方法上添加 `@Cacheable` 缓存注解接口，注意他有以以下参数（[参考博客](https://xie.infoq.cn/article/001e0f5ab65fa7dd1484c51e5)）
	- `cacheNames/value` ：用来指定缓存组件的名字
	- `key` ：缓存数据时使用的 key，可以用它来指定。默认是使用方法参数的值。（这个 key 你可以使用 spEL 表达式来编写）
	- `keyGenerator` ：key 的生成器，推荐重写。 key 和 keyGenerator 二选一使用
	- `cacheManager` ：可以用来指定缓存管理器。从哪个缓存管理器里面获取缓存。
	- `condition` ：可以用来指定符合条件的情况下才缓存
	- `unless` ：否定缓存。当 unless 指定的条件为 true ，方法的返回值就不会被缓存。当然你也可以获取到结果进行判断。（通过 `#result` 获取方法结果）
	- `sync` ：是否使用异步模式。

[SpringBoot系列之缓存使用教程](https://www.cnblogs.com/mzq123/p/12629142.html)介绍了其他相关的注解

---

### 自定义Redis缓存注解

自定义注解的实现，也就是使用AOP进行切片处理

这里我主要参考

- [Redis缓存系列--(五)自定义Redis缓存注解的使用](https://www.cnblogs.com/mr-ziyoung/p/13943508.html)
- [Redis（四）：自定义注解实现 redis缓存操作](https://blog.csdn.net/XiaoHanZuoFengZhou/article/details/99742581)

上面这个相对简单，下面这个相当复杂，不过我还是一下面这个为蓝本来写的

#### 自定义缓存注解RedisCache

因为是自定义的，所以可以根据自己的需求来进行变动，我按照我自己的想法进行了一定的改动

```java
package cn.livorth.functionallearning.common.cache;

import java.lang.annotation.*;

/**
 * @program: FunctionalLearning
 * @description: 缓存注解
 * 此注解还可以使用布隆过滤器，对数据库和缓存中都不存在的查询放进过滤器，防止缓存击穿攻击；
 * @author: livorth
 * @create: 2021-10-05 21:50
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
@Documented
public @interface RedisCache {

    // key的前缀
    String nameSpace() default "";

    // key
    String key() default "";

    // 设置过期时间，默认1分钟
    long expireTime() default 1 * 60 * 1000;

    // 是否为查询操作，如果为写入数据库的操作，该值需置为 false
    boolean read() default true;

}
```

#### 具体AOP代理方法类RedisCacheAspect

和常规的AOP代理方法类相同，主要的是其中实现的具体逻辑

1. 根据相关信息生成key
2. 通过key在redis中查询数据是非已经存在
	- 如果不存在，则去数据库中查找，如果数据库中都不存在，则要考虑内存穿透的问题了
	- 如果存在，则返回redis中对应的数据，同时记得需要反序列化，毕竟存储的时候已经统一JSON化了

其中有两个地方需要注意

1. key的生成方案，仅仅考传入的"key"作为key肯定是不够的，必然会重复，由于我暂时对springEL表达式不是很了解，所以我是用的是MD5加密同时进行拼接的方式来生成的
2. 反序列的过程，比如泛型的判断

```java
package cn.livorth.functionallearning.common.cache;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.TimeUnit;

/**
 * @program: FunctionalLearning
 * @description: Redis缓存切面操作
 * @author: livorth
 * @create: 2021-10-05 21:56
 **/
@Aspect
@Component
@Slf4j
public class RedisCacheAspect {

    @Resource
    private RedisHandler handler;

    @Pointcut(value = "@annotation(cn.livorth.functionallearning.common.cache.RedisCache)")
    public void redisCache() {
    }

    // 在使用 redisCache 注解的地方织入此切点
    @Around(value = "redisCache()")
    private Object saveCache(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {

        log.info("<======拦截到redisCache方法:{}.{}======>" ,
                proceedingJoinPoint.getTarget().getClass().getName(), proceedingJoinPoint.getSignature().getName());

        // 获取切入的方法对象
        // 这个m是代理对象的，没有包含注解
        Method m = ((MethodSignature) proceedingJoinPoint.getSignature()).getMethod();
        // this()返回代理对象，target()返回目标对象，目标对象反射获取的method对象才包含注解
        Method methodWithAnnotations = proceedingJoinPoint.getTarget().getClass().getDeclaredMethod(
                proceedingJoinPoint.getSignature().getName(), m.getParameterTypes());

        Object result;
        // 根据目标方法对象获取注解对象
        RedisCache annotation = methodWithAnnotations.getDeclaredAnnotation(RedisCache.class);

        // 解析key
        String key = parseKey(methodWithAnnotations, proceedingJoinPoint.getArgs(), annotation.key(), annotation.nameSpace());

        // 到redis中获取缓存
        log.info("<====== 通过key：{}从redis中查询 ======>", key);
        String cache = handler.getCache(key);
        if (cache == null) {
            log.info("<====== Redis 中不存在该记录，从数据库查找 ======>");
            // 若不存在，则到数据库中去获取
            result = proceedingJoinPoint.proceed();
            if (result != null) {
                // 从数据库获取后存入redis, 若有指定过期时间，则设置
                long expireTime = annotation.expireTime();
                if (expireTime != -1) {
                    handler.saveCache(key, result, expireTime, TimeUnit.SECONDS);
                } else {
                    handler.saveCache(key, result);
                }
            }
//            else {
//                // 这里可以做一个布隆过滤器的处理
//            }
            return result;
        } else {
            // 如果缓存中存在数据
            return deSerialize(m, cache);
        }
    }

    /**
     * 反序列化
     * @param m 原方法的对应信息
     * @param cache
     * @return
     */
    private Object deSerialize(Method m, String cache) {
        // 原方法的返回数据类型类
        Class returnTypeClass = m.getReturnType();
        log.info("从缓存中获取数据：{}，返回类型为：{}" , cache, returnTypeClass);
        Object object = null;
        // 原方法的返回数据类型类
        Type returnType = m.getGenericReturnType();
        // 判断是否是ParameterizedType的实例，即泛型
        if(returnType instanceof ParameterizedType){
            ParameterizedType type = (ParameterizedType) returnType;
            Type[] typeArguments = type.getActualTypeArguments();
            for(Type typeArgument : typeArguments){
                // 如果是泛型则需要将其中每个单独转换
                Class typeArgClass = (Class) typeArgument;
                log.info("<======获取到泛型:{}" , typeArgClass.getName());
                object = JSON.parseArray(cache, typeArgClass);
            }
        }else {
            // 不是泛型则直接转换
            object = JSON.parseObject(cache, returnTypeClass);
        }
        return object;
    }


    /**
     * 解析springEL表达式，生成key
     * @param method 原方法
     * @param argValues 输入参数
     * @param key key
     * @param nameSpace 命名空间
     * @return
     */
    private String parseKey(Method method, Object[] argValues, String key, String nameSpace) {
        // 创建解析器，但是Spring表达式语言我并不是很熟悉，所以这里暂时不考虑使用
//        ExpressionParser parser = new SpelExpressionParser();
//        Expression expression = parser.parseExpression(key);
//        EvaluationContext context = new StandardEvaluationContext();
//
//        // 添加参数
//        DefaultParameterNameDiscoverer discover = new DefaultParameterNameDiscoverer();
//        String[] parameterNames = discover.getParameterNames(method);
//        for (int i = 0; i < parameterNames.length; i++) {
//            context.setVariable(parameterNames[i], argValues[i]);
//        }
//        // 解析
//        return /*method.getName() + ":" +*/ nameSpace + expression.getValue(context).toString();

//        简单点也可以 命名空间+key+方法名+参数的MD5加密
        StringBuilder prefix = new StringBuilder();
        prefix.append(nameSpace).append(".").append(key);
        prefix.append(".").append(method.getName());
        StringBuilder sb = new StringBuilder();
        for (Object obj : argValues) {
            sb.append(obj.toString());
        }
        return prefix.append(DigestUtils.md5DigestAsHex(sb.toString().getBytes())).toString();
    }


    @Component
    class RedisHandler {

        @Resource
        RedisTemplate<String, String> cache;

        <T> void saveCache(String key, T t, long expireTime, TimeUnit unit) {
            String value = JSON.toJSONString(t);
            log.info("<====== 存入Redis 数据：{}", value);
            cache.opsForValue().set(key, value, expireTime, unit);
        }

        <T> void saveCache(String key, T t) {
            String value = JSON.toJSONString(t, SerializerFeature.WRITE_MAP_NULL_FEATURES);
            cache.opsForValue().set(key, value);
        }

        void removeCache(String key) {
            cache.delete(key);
        }

        String getCache(String key) {
            return cache.opsForValue().get(key);
        }

    }

}
```

注意这里面的RedisHandler其实可以使用之前自己写的redis封装类RedisUtils

然后就是布隆过滤器的使用，其实我只知道概念，具体使用暂时还不清楚

#### 进行测试

说是测试也只是加个注解的事情

UserController.java

```java
/**
* /user/page/3/5
* @param thePage 当前多少页
* @param pageSize 每页多少个
* @return
*/
@LogAnnotation(logModule = "user", logType = "select",  logDescription = "通过分页获取所有用户信息")
@RedisCache(nameSpace = "user", key = "getAllUserByPage")
@GetMapping("page/{thePage}/{pageSize}")
public List<User> getAllUserByPage(@PathVariable("thePage") int thePage, @PathVariable("pageSize") int pageSize){
    Page<User> page = new Page<>(thePage, pageSize);
    return userService.getAllUserByPage(page);
}
```

然后进行多次访问并查看日志信息

http://localhost:8888/user/page/3/5
![在这里插入图片描述](https://img-blog.csdnimg.cn/2ed10aaa10d143fe84294e784804906b.png?x-oss-process=image/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBAQWxpdm9ydGg=,size_20,color_FFFFFF,t_70,g_se,x_16)![在这里插入图片描述](https://img-blog.csdnimg.cn/abb633070e55476e87cfd88cb9343fb2.png?x-oss-process=image/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBAQWxpdm9ydGg=,size_20,color_FFFFFF,t_70,g_se,x_16)
第一次是对数据库中信息进行访问，第二次测试从缓存中找

---

**写在后面**

注解自定义，代理方法类自己实现，很多东西的决定权在自己手上

要想简单写可以写的很简单，要写复杂那都不是几百行可以搞定的

只能说看需求来吧，我这也只能起到一个抛砖引玉的作用

---

我的项目demo系列都会同步到GitHub上，欢迎围观

https://github.com/Livorth/FunctionalLearning