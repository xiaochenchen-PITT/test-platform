package com.cxc.test.platform.common.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class ResultDO<T> implements Serializable {

    private Boolean isSuccess;

    private T data;

    private String errorMessage;

    private ResultDO(T data, Boolean isSuccess, String errorMessage) {
        this.data = data;
        this.isSuccess = isSuccess;
        this.errorMessage = errorMessage;
    }

    public static <T> ResultDO<T> success(T data) {
        return new ResultDO<>(data, true, null);
    }

    public static <T> ResultDO<T> fail(String errorMessage) {
        return new ResultDO<>(null, false, errorMessage);
    }

    @Override
    public String toString() {
        return "ResultDO{" +
            "isSuccess=" + isSuccess +
            ", data=" + data +
            ", errorMessage=" + errorMessage +
            '}';
    }

}
