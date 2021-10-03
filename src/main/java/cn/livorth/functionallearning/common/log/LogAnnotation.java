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
