package com.secondhand.exception;

import com.secondhand.dto.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常：{}", e.getMessage());
        return Result.error(e.getMessage());
    }

    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException e) {
        String message = e.getAllErrors().get(0).getDefaultMessage();
        return Result.error(400, "参数错误：" + message);
    }

    @ExceptionHandler(RuntimeException.class)
    public Result<Void> handleRuntimeException(RuntimeException e) {
        log.error("业务异常：{}", e.getMessage());
        return Result.error(e.getMessage());
    }
}