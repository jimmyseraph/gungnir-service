package vip.testops.gungnir.commons;

import lombok.Data;

/**
 * 服务端API返回的JSON统一格式
 * @param <T>
 */
@Data
public class Response<T> {
    private Integer code;
    private String message;
    private T data;

    public void commonSuccess() {
        setCode(BaseCodeEnum.COMMON_SUCCESS.getCode());
        setMessage(BaseCodeEnum.COMMON_SUCCESS.getDescEN());
    }

    public void dataSuccess(T data) {
        setCode(BaseCodeEnum.COMMON_SUCCESS.getCode());
        setMessage(BaseCodeEnum.COMMON_SUCCESS.getDescEN());
        setData(data);
    }

    public void paramMissError(String paramName) {
        setCode(BaseCodeEnum.PARAM_MISS.getCode());
        setMessage(String.format(BaseCodeEnum.PARAM_MISS.getDescEN(), paramName));
    }

    public void paramIllegalError(String paramName) {
        setCode(BaseCodeEnum.PARAM_ILLEGAL.getCode());
        setMessage(String.format(BaseCodeEnum.PARAM_ILLEGAL.getDescEN(), paramName));
    }

    public void serviceError(String reason){
        setCode(BaseCodeEnum.SERVICE_ERROR.getCode());
        setMessage(String.format(BaseCodeEnum.SERVICE_ERROR.getDescEN(), reason));
    }

    public void unauthorizedError(){
        setCode(BaseCodeEnum.ACCESS_DENIED.getCode());
        setMessage(BaseCodeEnum.ACCESS_DENIED.getDescEN());
    }

}
