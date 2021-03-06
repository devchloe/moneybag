package com.kakaopay.moneydistribution.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(BizException.class)
    public ResponseEntity<ErrorResponse> handleMoneyBagNotFoundException(BizException ex) {

        return new ResponseEntity<>(getErrorResponse(ex), HttpStatus.valueOf(ex.getStatus()));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return new ResponseEntity<>(getErrorResponse(status.value(), ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    private ErrorResponse getErrorResponse(BizException ex) {
        return getErrorResponse(ex.getStatus(), ex.getMessage());
    }

    private ErrorResponse getErrorResponse(int status, String message) {
        return ErrorResponse.builder().status(status).message(message).build();
    }
}
