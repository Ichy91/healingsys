package com.healingsys.exceptions;

public class ApiNotCompletedException extends RuntimeException{

    public ApiNotCompletedException(String message) {
        super(message);
    }
}
