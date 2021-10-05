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
