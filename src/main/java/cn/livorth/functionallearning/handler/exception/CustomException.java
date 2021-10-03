package cn.livorth.functionallearning.handler.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @program: FunctionalLearning
 * @description: 自定义异常类
 * @author: livorth
 * @create: 2021-10-03 11:49
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomException extends RuntimeException {

    private Integer code;

    private String msg;

}
