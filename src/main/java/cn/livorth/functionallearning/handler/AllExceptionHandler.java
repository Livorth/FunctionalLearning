package cn.livorth.functionallearning.handler;

import cn.livorth.functionallearning.constant.ErrorCode;
import cn.livorth.functionallearning.entity.dto.ResultDTO;
import cn.livorth.functionallearning.handler.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;


/**
 * @program: blog
 * @description: 统一异常处理类
 * @author: livorth
 * @create: 2021-09-29 20:17
 **/
@ControllerAdvice
@Slf4j
public class AllExceptionHandler {
    /**
     * 进行异常处理，处理Exception.class的异常
     * 然后返回的是json类型的数据
     */
//    @ExceptionHandler(Exception.class)
//    @ResponseBody
//    public ResultDTO doAllException(Exception e){
//        log.info("出现了异常：" + e.getMessage());
//        return ResultDTO.byErrorCode(ErrorCode.SYSTEM_ERROR);
//    }

    /**
     * 拦截参数校验异常
     */
    @ExceptionHandler(ArithmeticException.class)
    @ResponseBody
    public ResultDTO doException(Exception e){
        log.info("出现了异常：" + e.getMessage());
        return ResultDTO.fail(505, "测试特殊异常处理");
    }

    /**
     * 拦截参自定义异常
     */
    @ExceptionHandler(CustomException.class)
    @ResponseBody
    public ResultDTO doCustomException(CustomException e){
        log.info("出现了异常：" + e.getMessage());
        return ResultDTO.fail(e.getCode(), e.getMsg());
    }

}
