package com.aron.jcore.rest.exception;

public class ExceptionMessage {
    public int statusCode;
    public String errorMessage;

    public ExceptionMessage(int statusCode, String errorMessage) {
        this.statusCode = statusCode;
        this.errorMessage = errorMessage;
    }
}
