package org.example.edusoft.common.domain;

import lombok.Data;

/**
 * 响应数据
 *
 * @Author: sjy
 * @Date: 2025/5/15 
 */
@Data
public class Result<T> {
    private int code;

    private String msg = "success";

    private T data;

    public static <T> Result<T> ok(String msg) {
        return ok(null, 200, msg);
    }

    public static <T> Result<T> ok(T model, String msg) {
        return ok(model, 200, msg);
    }


    public static <T> Result<T> ok(T datas, Integer code, String msg) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        result.setData(datas);
        return result;
    }

    public static <T> Result<T> error() {
        return error(500, "未知异常");
    }

    public static <T> Result<T> error(String msg) {
        return error(500, msg);
    }

    public static <T> Result<T> error(int code, String msg) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }

    public static <T> Result<T> success(T data, String msg) {
        Result<T> result = new Result<>();
        result.setCode(0);
        result.setMsg(msg);
        result.setData(data);
        return result;
    }

}
