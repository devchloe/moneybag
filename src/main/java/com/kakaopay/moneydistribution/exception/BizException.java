package com.kakaopay.moneydistribution.exception;

import lombok.Getter;

@Getter
public class BizException extends RuntimeException {
    private int status;

    public BizException(String message, int status) {
        super(message);
        this.status = status;
    }

}
