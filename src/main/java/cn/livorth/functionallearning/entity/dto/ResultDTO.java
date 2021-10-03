package cn.livorth.functionallearning.entity.dto;

import cn.livorth.functionallearning.constant.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @program: FunctionalLearning
 * @description: 结果返回
 * @author: livorth
 * @create: 2021-10-03 11:17
 **/
@Data
@AllArgsConstructor
public class ResultDTO {
    private Boolean success;

    private Integer code;

    private String msg;

    private Object data;


    public static ResultDTO success(Object data){
        return new ResultDTO(true,200,"success", data);
    }

    public static ResultDTO fail(Integer code, String msg){
        return new ResultDTO(false,code,msg,null);
    }

    public static ResultDTO byErrorCode(ErrorCode errorCode){
        return new ResultDTO(false, errorCode.getCode(), errorCode.getMsg(),null);
    }
}
