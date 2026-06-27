package com.resume.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * 统一响应结果封装。
 * 所有 API 统一返回此类型，前端拦截器统一解析。
 *
 * @param <T> 数据类型
 */
public class Result<T> {

    /** 状态码，200 表示成功 */
    private int code;
    /** 提示信息 */
    private String message;
    /** 响应数据 */
    private T data;

    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
    }

    public static <T> Result<T> success() {
        return new Result<>(200, "success", null);
    }

    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null);
    }

    public static <T> Result<T> error(String message) {
        return new Result<>(500, message, null);
    }
}
