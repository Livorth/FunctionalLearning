package cn.livorth.functionallearning.constant;

/**
 * @program: ErrorCode
 * @description: 错误码
 * @author: livorth
 * @create: 2021-09-28 17:24
 **/
public enum ErrorCode {
    /**
     * 不同种类的错误码
     */
    PARAMS_ERROR(10001,"参数有误"),
    ACCOUNT_PWD_NOT_EXIST(10002,"用户名或密码不存在"),
    TOKEN_ERROR(10003,"token不合法"),
    ACCOUNT_EXIST(10004,"账号已存在"),
    NO_PERMISSION(70001,"无访问权限"),
    SESSION_TIME_OUT(90001,"会话超时"),
    NO_LOGIN(90002,"未登录"),
    SYSTEM_ERROR(505, "系统出了点小问题，这不找管理员并ta一顿？");

    private Integer code;
    private String msg;

    ErrorCode(Integer code, String msg){
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
