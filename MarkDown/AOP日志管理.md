## AOP日志管理

AOP是个好东西，用的地方很多，但是这里仅介绍作为日志管理的使用

总的来说，使用AOP的思路是差不多的

1. 使用自定义注解
2. 使用AOP环绕通知代理有对应注解的方法，然后在环绕的时候进行对应操作
3. 在需要使用日志方法上加上注解，进行测试

在这里我并不会将日志信息入库，而是直接打印在控制台（因为入库麻烦但是容易实现）

我主要参考的 [SpringBoot整合aop日志管理](https://www.jianshu.com/p/59ca84fadbaf)

### 使用自定义注解

注解一般相当于一个标记，一个传递信息的作用

LogAnnotation.java

```java
package cn.livorth.functionallearning.common.log;

import java.lang.annotation.*;

/**
 * @program: blog
 * @description: 日志注解
 * @author: livorth
 * @create: 2021-09-30 18:31
 **/
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogAnnotation {
    // 操作模块
    String logModule() default "";
    // 操作类型
    String logType() default "";
    // 操作信息
    String logDescription() default "";
}
```

### AOP环绕通知类

其实除了环绕`@Around:`，还可以选择`@Before`、`@After`、`@AfterRunning`、`@AfterThrowing`

比较推荐用`@AfterThrowing`，对异常进行特殊处理

在我的日志方法中，我并没有采用将日志记录进数据库，但是可以加上，比较容易写

```java
package cn.livorth.functionallearning.common.log;

/**
 * @program: FunctionalLearning
 * @description: 日志切片类
 * @author: livorth
 * @create: 2021-10-03 20:05
 **/

import cn.livorth.functionallearning.utils.HttpContextUtils;
import cn.livorth.functionallearning.utils.IpUtils;
import lombok.extern.slf4j.Slf4j;
import com.alibaba.fastjson.JSON;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

/**
 * 切面处理类，操作日志异常日志记录处理
 *
 * @author wu
 * @date 2019/03/21
 */
@Aspect
@Component
@Slf4j
public class LogAspect {

    /**
     * PointCut表示这是一个切点，@annotation表示这个切点切到一个注解上，后面带该注解的全类名
     * 切面最主要的就是切点，所有的故事都围绕切点发生
     * logPointCut()代表切点名称
     */
    @Pointcut("@annotation(cn.livorth.functionallearning.common.log.LogAnnotation)")
    public void logPointCut(){}
    
    /**
     * 环绕通知
     * @param joinPoint，这个参数里面可以获取到所加注解方法的详细信息，包括方法名、入参出参等信息
     * @return
     * @throws Throwable
     */
    @Around("logPointCut()")
    public Object log(ProceedingJoinPoint joinPoint) throws Throwable {
        long beginTime = System.currentTimeMillis();
        //执行方法
        Object result = joinPoint.proceed();
        //执行时长(毫秒)
        long time = System.currentTimeMillis() - beginTime;
        //保存日志
        recordLog(joinPoint, JSON.toJSONString(result), time);
        return result;
    }

    /**
     * 对日志信息进行操作
     * @param joinPoint 切点
     * @param time 方法执行时间
     */
    private void recordLog(ProceedingJoinPoint joinPoint, String result, long time) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        LogAnnotation logAnnotation = method.getAnnotation(LogAnnotation.class);

        // 日志记录
        log.info("=====================log start================================");
        // 日志基础信息
        log.info("module:{}",logAnnotation.logModule());
        log.info("type:{}",logAnnotation.logType());
        log.info("description:{}",logAnnotation.logDescription());

        // 请求的方法名
        String className = joinPoint.getTarget().getClass().getName();
        String methodName = signature.getName();
        log.info("request method:{}",className + "." + methodName + "()");

        // 请求的参数
        Object[] args = joinPoint.getArgs();
        String params = JSON.toJSONString(args);
        log.info("params:{}",params);

        // 返回的结果
        log.info("result:{}",result);

        // 获取request 设置IP地址
        HttpServletRequest request = HttpContextUtils.getHttpServletRequest();
        log.info("ip:{}", IpUtils.getIpAddress(request));


        log.info("execution time : {} ms",time);
        log.info("=====================log end================================");

        // 这里可以将相关信息提取出来进行一个入库操作
    }
}
```

### 涉及到IP的处理

使用RequestContextHolder可以在Controller中获取request对象

然后可以使用request在请求头中找到对应的IP信息

这个部分是直接抄的，看起来大家都是通用的

HttpContextUtils.java

```java
package cn.livorth.functionallearning.utils;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * HttpServletRequest
 * @author Livorth
 */
public class HttpContextUtils {

    // 在Controller中获取request对象
    public static HttpServletRequest getHttpServletRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    }

}
```

IpUtils.java 

```java
package cn.livorth.functionallearning.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import javax.servlet.http.HttpServletRequest;

/**
 * 获取Ip
 * @author Livorth
 */
@Slf4j
public class IpUtils {

    /**
     * 获取IP地址
     * 使用Nginx等反向代理软件， 则不能通过request.getRemoteAddress()获取IP地址
     * 如果使用了多级反向代理的话，X-Forwarded-For的值并不止一个，而是一串IP地址，X-Forwarded-For中第一个非unknown的有效IP字符串，则为真实IP地址
     */
    public static String getIpAddress(HttpServletRequest request) {
        String ip = null;
        String unknown = "unknown";
        String seperator = ",";
        int maxLength = 15;
        try {
            ip = request.getHeader("x-forwarded-for");
            if (StringUtils.isEmpty(ip) || unknown.equalsIgnoreCase(ip)) {
                ip = request.getHeader("Proxy-Client-IP");
            }
            if (StringUtils.isEmpty(ip) || ip.length() == 0 || unknown.equalsIgnoreCase(ip)) {
                ip = request.getHeader("WL-Proxy-Client-IP");
            }
            if (StringUtils.isEmpty(ip) || unknown.equalsIgnoreCase(ip)) {
                ip = request.getHeader("HTTP_CLIENT_IP");
            }
            if (StringUtils.isEmpty(ip) || unknown.equalsIgnoreCase(ip)) {
                ip = request.getHeader("HTTP_X_FORWARDED_FOR");
            }
            if (StringUtils.isEmpty(ip) || unknown.equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }
        } catch (Exception e) {
            log.error("IpUtils ERROR ", e);
        }

        // 使用代理，则获取第一个IP地址
        if (StringUtils.isEmpty(ip) && ip.length() > maxLength) {
            int idx = ip.indexOf(seperator);
            if (idx > 0) {
                ip = ip.substring(0, idx);
            }
        }
        return ip;
    }

    /**
     * 获取ip地址
     *
     * @return
     */
    public static String getIpAddress() {
        HttpServletRequest request = HttpContextUtils.getHttpServletRequest();
        return getIpAddress(request);
    }
}
```

### 测试使用

分别在 `getAllUser()` 与 `getAllUserByPage()` 上加上 `@LogAnnotation`，并进行测试

```java
@LogAnnotation(logModule = "user", logType = "select",  logDescription = "获取所有用户信息")
@GetMapping
public List<User> getAllUser(){
    return userService.getAllUser();
}

/**
     * /user/page/3/5
     * @param thePage 当前多少页
     * @param pageSize 每页多少个
     * @return
     */
@LogAnnotation(logModule = "user", logType = "select",  logDescription = "通过分页获取所有用户信息")
@GetMapping("page/{thePage}/{pageSize}")
public List<User> getAllUserByPage(@PathVariable("thePage") int thePage, @PathVariable("pageSize") int pageSize){
    Page<User> page = new Page<>(thePage, pageSize);
    return userService.getAllUserByPage(page);
}
```

![\[外链图片转存失败,源站可能有防盗链机制,建议将图片保存下来直接上传(img-T5IcWT4q-1633266314479)(Functional%20learning.assets/image-20211003210154880.png)\]](https://img-blog.csdnimg.cn/a53511c3a1b24c0ba4ddb1e7c5b15ed0.png?x-oss-process=image/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBAQWxpdm9ydGg=,size_20,color_FFFFFF,t_70,g_se,x_16)


---

**写在后面**

日志这种东西需要根据项目进行调整，毕竟很多的日志信息其实是无效信息，如果将这些东西存入数据库，只会占用服务器的资源，所以根据需求来，进行动态的调整才是最合适的

---

我的项目demo系列都会同步到GitHub上，欢迎围观

https://github.com/Livorth/FunctionalLearning

