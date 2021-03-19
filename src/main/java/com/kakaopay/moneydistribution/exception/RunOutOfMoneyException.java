package com.kakaopay.moneydistribution.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class RunOutOfMoneyException extends BizException {

    public RunOutOfMoneyException() {
        super("돈이 모두 소진되었습니다.", HttpStatus.CONFLICT.value());
    }
}
