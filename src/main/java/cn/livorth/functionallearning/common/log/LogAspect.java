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
