package com.teletrader.stockorderservice.controller;

import com.teletrader.stockorderservice.exception.BadRequestException;
import com.teletrader.stockorderservice.exception.ErrorResponse;
import com.teletrader.stockorderservice.exception.InternalServerErrorException;
import com.teletrader.stockorderservice.exception.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ExceptionHandlerController {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new HashMap<>();
        for (ObjectError error : exception.getBindingResult().getAllErrors()) {
            String fieldName = (error instanceof FieldError) ? ((FieldError) error).getField() : "global";
            errors.put(fieldName, error.getDefaultMessage());
        }
        return errors;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({BadRequestException.class, ValidationException.class})
    public ErrorResponse handleBadRequestExceptions(RuntimeException exception) {
        return new ErrorResponse(exception.getMessage(), "Bad Request");
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(InternalServerErrorException.class)
    public ErrorResponse handleInternalServerError(InternalServerErrorException exception) {
        return new ErrorResponse(exception.getMessage(), "Internal Server Error");
    }
}
