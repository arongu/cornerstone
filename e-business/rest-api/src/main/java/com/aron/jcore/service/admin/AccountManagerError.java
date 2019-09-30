package com.aron.jcore.service.admin;

public class AccountManagerError {
    public String email, cause;

    public AccountManagerError(String email, String cause) {
        this.email = email;
        this.cause = cause;
    }

    @Override
    public String toString() {
        return "{" +
                "\"email\": \"" + email + "\"," +
                "\"cause\": \"" + cause + "\"}";
    }
}
