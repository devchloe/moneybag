package com.kakaopay.moneydistribution.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class MoneyBagExpiredException extends BizException {

    public MoneyBagExpiredException(String message) {
        super(message, HttpStatus.CONFLICT.value());
    }
}
