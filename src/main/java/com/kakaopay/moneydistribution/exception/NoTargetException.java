package com.kakaopay.moneydistribution.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class NoTargetException extends BizException {

    public NoTargetException(String message) {
        super(message, HttpStatus.BAD_REQUEST.value());
    }
}
