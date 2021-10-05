## SpringBoot整合Redis

Redis是目前使用最多的非关系型数据库，如果要详细学的话，都可以开一个新系列了

我的这篇博客构建的基础是在你大致掌握了Redis之上来写的

虽然是这么说，我自己其实对redis的了解也不是很深刻，所以暂时仅了解如何使用

我的主要参考来源：

- [springboot集成redis(基础篇)](https://zhuanlan.zhihu.com/p/139528556)
- [SpringBoot集成Redis](https://www.cnblogs.com/vchar/p/14591566.html)

- [SpringBoot整合Redis及Redis工具类撰写](https://www.cnblogs.com/zeng1994/p/03303c805731afc9aa9c60dbbd32a323.html)

---

### 添加依赖与yaml配置

不管怎么样使用什么新的东西总还是要添加配置与对应配置，然后再是自定义配置与使用

1. 添加依赖

	```xml
	<!--    组合redis-->
	<dependency>
	    <groupId>org.springframework.boot</groupId>
	    <artifactId>spring-boot-starter-data-redis</artifactId>
	</dependency>
	<!-- 使用lettuce做redis的连接池需要额外引入这个包-->
	<dependency>
	    <groupId>org.apache.commons</groupId>
	    <artifactId>commons-pool2</artifactId>
	</dependency>
	```

	如果你不需要连接池，那么不用加上lettuce

2. 修改对应的yaml配置

	```yaml
	  redis:
	    # 地址
	    host: localhost
	    # 端口号
	    port: 6379
	    # 密码
	    password: ***
	    # 超时时间，单位毫秒
	    timeout: 3000
	    # 数据库编号
	    database: 0
	    # 配置lettuce
	    lettuce:
	      pool:
	        # 连接池中的最小空闲连接
	        min-idle: 1
	        # 连接池中的最大空闲连接
	        max-idle: 6
	        # 连接池最大连接数（使用负值表示没有限制,不要配置过大，否则可能会影响redis的性能）
	        max-active: 10
	        # 连接池最大阻塞等待时间（使用负值表示没有限制）；单位毫秒
	        max-wait: 1000
	      #关闭超时时间；单位毫秒
	      shutdown-timeout: 200
	```

	不过实际上要说的话，其实只需要host、port、password 就可以开始使用了，其他的配置只是列出来，要配的时候知道有就好了

### 使用已有的RedisTemplate

springboot的redis自动装配类已经配置了一个StringRedisTemplate的bean

但是上述bean有限制条件，那就是key与value都是String，不过也可以直接使用RedisTemplate，那就是<key, value> => <Object, Object>，更不方便使用

```java
public class RedisTemplate<K, V> extends RedisAccessor implements RedisOperations<K, V>, BeanClassLoaderAware {}

public class StringRedisTemplate extends RedisTemplate<String, String>{}
```

当需要拓展功能的话就只能自己写RedisTemplate了，这个在后续会提及

这里仅简单测试一下其是否能正常连接redis，以及一般<key, value> => <String, String>的使用

```java
package cn.livorth.functionallearning;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

@SpringBootTest
class FunctionalLearningApplicationTests {

    @Autowired
    private StringRedisTemplate strRedis;

    @Test
    void redisTestStringSet(){
        strRedis.opsForValue().set("RedisTest", "Test");
        strRedis.opsForValue().set("RedisTest中文", "中文测试");
    }

    @Test
    void redisTestStringGet(){
        String redisTest = strRedis.opsForValue().get("RedisTest");
        System.out.println(redisTest);
    }
}
```
![在这里插入图片描述](https://img-blog.csdnimg.cn/a5d9fa5c85a2436ea75df63d5951caea.png?x-oss-process=image/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBAQWxpdm9ydGg=,size_20,color_FFFFFF,t_70,g_se,x_16)![在这里插入图片描述](https://img-blog.csdnimg.cn/9ef6895314b74f5b83ef14b5031a7f32.png)

像这种key与value都是String的使用情况，其实可以不用再自己重写RedisTemplate，作为简单的使用还是不错的选择

### 自定义配置RedisTemplate

在我参考的博客中，主要有两种不同复杂度的配置方案

1. 方案一：更普遍，更简单

	这里参考博客：https://zhuanlan.zhihu.com/p/139528556

	```java
	@Configuration
	public class RedisConfig {
	    @Bean
	    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
	        // 创建redisTemplate
	        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
	        redisTemplate.setConnectionFactory(connectionFactory);
	        // 使用Jackson2JsonRedisSerialize替换默认序列化
	        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
	        ObjectMapper objectMapper = new ObjectMapper();
	        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
	        objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
	
	        jackson2JsonRedisSerializer.setObjectMapper(objectMapper);
	
	        // key采用String的序列化方式
	        redisTemplate.setKeySerializer(new StringRedisSerializer());
	        // value序列化方式采用jackson
	        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
	        // hash的key也采用String的序列化方式
	        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
	        // hash的value序列化方式采用jackson
	        redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);
	        redisTemplate.afterPropertiesSet();
	        return redisTemplate;
	    }
	```

2. 方案二：结合连接池，为后续的redis集群做准备，相对复杂

	这里参考博客：https://www.cnblogs.com/vchar/p/14591566.html

	```java
	package cn.livorth.functionallearning.config;
	
	import com.fasterxml.jackson.annotation.JsonAutoDetect;
	import com.fasterxml.jackson.annotation.PropertyAccessor;
	import com.fasterxml.jackson.databind.ObjectMapper;
	import com.fasterxml.jackson.databind.SerializationFeature;
	import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
	import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
	import org.springframework.cache.CacheManager;
	import org.springframework.cache.annotation.CachingConfigurerSupport;
	import org.springframework.cache.interceptor.KeyGenerator;
	import org.springframework.context.annotation.Bean;
	import org.springframework.context.annotation.Configuration;
	import org.springframework.data.redis.cache.RedisCacheConfiguration;
	import org.springframework.data.redis.cache.RedisCacheManager;
	import org.springframework.data.redis.cache.RedisCacheWriter;
	import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
	import org.springframework.data.redis.core.RedisTemplate;
	import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
	import org.springframework.data.redis.serializer.RedisSerializer;
	import org.springframework.data.redis.serializer.StringRedisSerializer;
	import org.springframework.util.DigestUtils;
	
	import java.time.Duration;
	
	/**
	 * @program: FunctionalLearning
	 * @description: redis配置类
	 * @author: livorth
	 * @create: 2021-10-05 16:21
	 **/
	@Configuration
	public class RedisConfig<K, V> extends CachingConfigurerSupport {
	
	    /**
	     * 自定义缓存注解key的生成策略。默认的生成策略是看不懂的(乱码内容)
	     * 通过Spring 的依赖注入特性进行自定义的配置注入并且此类是一个配置类可以更多程度的自定义配置
	     * 这里是生成的key是：类全名.方法名 方法参数（的md5加密）
	     */
	    @Bean
	    @Override
	    public KeyGenerator keyGenerator() {
	        return (target, method, params) -> {
	            StringBuilder prefix = new StringBuilder();
	            prefix.append(target.getClass().getName());
	            prefix.append(".").append(method.getName());
	            StringBuilder sb = new StringBuilder();
	            for (Object obj : params) {
	                sb.append(obj.toString());
	            }
	            return prefix.append(DigestUtils.md5DigestAsHex(sb.toString().getBytes()));
	        };
	    }
	
	    /**
	     * 缓存配置管理器
	     */
	    @Bean
	    public CacheManager cacheManager(LettuceConnectionFactory factory) {
	        // 以锁写入的方式创建RedisCacheWriter对象
	        RedisCacheWriter writer = RedisCacheWriter.lockingRedisCacheWriter(factory);
	        // 设置缓存注解的缓存时间，缓存1小时
	        Duration duration = Duration.ofSeconds(3600L);
	        RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig().entryTtl(duration);
	        return new RedisCacheManager(writer, redisCacheConfiguration);
	    }
	
	    /**
	     * 修改redisTemplate的序列化方式
	     * @param factory LettuceConnectionFactory，即连接池的链接方式
	     */
	    @Bean(name = "redisTemplate")
	    public RedisTemplate<K, V> redisTemplate(LettuceConnectionFactory factory) {
	        //创建RedisTemplate对象
	        RedisTemplate<K, V> template = new RedisTemplate<K, V>();
	        template.setConnectionFactory(factory);
	        //设置key的序列化方式
	        template.setKeySerializer(keySerializer());
	        template.setHashKeySerializer(keySerializer());
	
	        //设置RedisTemplate的Value序列化方式Jackson2JsonRedisSerializer；默认是JdkSerializationRedisSerializer
	        template.setValueSerializer(valueSerializer());
	        template.setHashValueSerializer(valueSerializer());
	
	        template.afterPropertiesSet();
	        return template;
	    }
	
	    /**
	     * key采用String的序列化方式
	     * @return
	     */
	    private RedisSerializer<String> keySerializer() {
	        return new StringRedisSerializer();
	    }
	
	    /**
	     * value序列化方式采用jackson
	     * @return
	     */
	    private RedisSerializer<Object> valueSerializer() {
	        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<Object>(Object.class);
	
	        ObjectMapper om = new ObjectMapper();
	        // 指定要序列化的域，field,get和set,以及修饰符范围，ANY是都有包括private和public
	        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
	        // 指定序列化输入的类型，类必须是非final修饰的，final修饰的类，比如String,Integer等会抛出异常
	        om.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);
	        //解决时间序列化问题
	        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
	        om.registerModule(new JavaTimeModule());
	
	        jackson2JsonRedisSerializer.setObjectMapper(om);
	        return jackson2JsonRedisSerializer;
	    }
	
	}
	```

其实这两种方案实际上的配置是一样的，区别在于使用的 ConnectionFactory 不同，这里不做深入讨论，建议自行百度了解

做这一步的配置主要原因是修改redisTemplate的序列化方式，redisTemplate原本的序列方式是JdkSerializationRedisSerializer，这里我们修改为Jackson2JsonRedisSerializer，也就是可以将传入的对象格式化为JSON。

### 简单测试

写两个接口操作redis

UserController.java

```java
@GetMapping("setInCache/{id}")
public Boolean setAllUserInCacheById(@PathVariable("id") int id){
    return userService.setAllUserInCacheById(id);
}

@GetMapping("getInCache/{id}")
public User getAllUserInCacheById(@PathVariable("id") int id){
    return userService.getAllUserByCacheById(id);
}
```

 UserServiceImpl.java

```java
@Override
public Boolean setAllUserInCacheById(int id) {
    User user = userMapper.selectById(id);
    redisTemplate.opsForValue().set("UserInfo:" + id, user);
    return true;
}

@Override
public User getAllUserByCacheById(int id) {
    User user = (User) redisTemplate.opsForValue().get("UserInfo:" + id);
    return user;
}
```

然后分别访问

- [http://localhost:8888/user/setInCache/1](http://localhost:8888/user/setInCache/1)
- [http://localhost:8888/user/getInCache/1](http://localhost:8888/user/getInCache/1)

![在这里插入图片描述](https://img-blog.csdnimg.cn/7603d0dd5440449697718eded603401b.png?x-oss-process=image/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBAQWxpdm9ydGg=,size_20,color_FFFFFF,t_70,g_se,x_16)
![啊](https://img-blog.csdnimg.cn/77f0aac7fd4c467d938007c0fb72c47e.png?x-oss-process=image/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBAQWxpdm9ydGg=,size_20,color_FFFFFF,t_70,g_se,x_16)
上传和获取都很成功

### 封装工具类

这里我采用的[java redisUtils工具类很全](https://www.cnblogs.com/zhzhlong/p/11434284.html)中封装的工具类，太长了就不在复制粘贴了

不过这种工具类得按照自己的需求来，很多东西得自己改或者自己写，随机应变吧

当你的项目使用redis操作较多而且比较复杂的时候确实可以封装个工具类，但是只是随便搞点String-String缓存我觉得甚至都没必要自定义配置RedisTemplate

---

**写在后面**

最开始我以为Redis的整合很简单，但是当真正了解之后才知道自己目前知道的东西只是冰山一角，但是受篇幅限制这里只能简单介绍如何在SpringBoot中使用Redis，实际上还有很多内容可以单独拎出来说，比如

- Redis集群连接配置以及读写分离：https://www.cnblogs.com/vchar/p/14591566.html
- 高并发下穿透问题处理：https://www.cnblogs.com/shenlailai/p/10583869.html

- 数据库同步
- ....

还是以后时间足够再深入了解吧

---

我的项目demo系列都会同步到GitHub上，欢迎围观

https://github.com/Livorth/FunctionalLearning

