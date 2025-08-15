package com.example.exception;

public class OpenWeatherException extends RuntimeException {
    public OpenWeatherException(String msg) {
        super(msg);
    }

    public OpenWeatherException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
