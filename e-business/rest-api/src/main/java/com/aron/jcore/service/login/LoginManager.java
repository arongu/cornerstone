package com.aron.jcore.service.login;

public interface LoginManager {
    boolean authenticate(final String email, final String password);
    String issueJWTtoken(final String email);
}
