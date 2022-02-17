package com.healingsys.exceptions;

public class ApiAlreadyExistException extends RuntimeException{

    public ApiAlreadyExistException(String message) {
        super(message);
    }
}
