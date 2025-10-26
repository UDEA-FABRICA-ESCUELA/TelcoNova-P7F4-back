package com.telconova.suportsuite.exception;

// Simplemente extiende de RuntimeException
public class LockedAccountException extends RuntimeException {
    public LockedAccountException(String message) {
        super(message);
    }
}