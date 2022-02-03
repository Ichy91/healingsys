package com.healingsys.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.ZonedDateTime;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(value = {ApiNoSuchElementException.class})
    public ResponseEntity<Object> handleApiNoSuchElementException(ApiNoSuchElementException e) {
        HttpStatus notFound = HttpStatus.NOT_FOUND;

        ApiException apiException = new ApiException(
                e.getMessage(),
                notFound,
                ZonedDateTime.now()
        );

        return new ResponseEntity<>(apiException, notFound);
    }

    @ExceptionHandler(value = {ApiAlreadyExistException.class})
    public ResponseEntity<Object> handleApiAlreadyExistException(ApiAlreadyExistException e) {
        HttpStatus alreadyExist = HttpStatus.CONFLICT;

        ApiException apiException = new ApiException(
                e.getMessage(),
                alreadyExist,
                ZonedDateTime.now()
        );

        return new ResponseEntity<>(apiException, alreadyExist);
    }

    @ExceptionHandler(value = {ApiIllegalArgumentException.class})
    public ResponseEntity<Object> handleApiIllegalArgumentException(ApiIllegalArgumentException e) {
        HttpStatus illegalArgument = HttpStatus.BAD_REQUEST;

        ApiException apiException = new ApiException(
                e.getMessage(),
                illegalArgument,
                ZonedDateTime.now()
        );

        return new ResponseEntity<>(apiException, illegalArgument);
    }

    @ExceptionHandler(value = {ApiIllegalAccessException.class})
    public ResponseEntity<Object> handleApiIllegalAccessException(ApiIllegalAccessException e) {
        HttpStatus illegalAccess = HttpStatus.UNAUTHORIZED;

        ApiException apiException = new ApiException(
                e.getMessage(),
                illegalAccess,
                ZonedDateTime.now()
        );

        return new ResponseEntity<>(apiException, illegalAccess);
    }
}
