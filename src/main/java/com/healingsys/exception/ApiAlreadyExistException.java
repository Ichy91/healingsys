package com.healingsys.exception;

public class ApiAlreadyExistException extends RuntimeException{

    public ApiAlreadyExistException(String message) {
        super(message);
    }
}
