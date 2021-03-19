package com.kakaopay.moneydistribution.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class MoneyBagNotFoundException extends BizException {

    public MoneyBagNotFoundException() {
        super("유효하지 않은 뿌리기 건 입니다.", HttpStatus.NOT_FOUND.value());
    }
}
