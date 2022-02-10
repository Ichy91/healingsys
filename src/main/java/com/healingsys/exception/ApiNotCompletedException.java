package com.healingsys.exception;

public class ApiNotCompletedException extends RuntimeException{

    public ApiNotCompletedException(String message) {
        super(message);
    }
}
