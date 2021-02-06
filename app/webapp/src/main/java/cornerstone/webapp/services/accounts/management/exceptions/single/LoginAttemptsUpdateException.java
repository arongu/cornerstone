package cornerstone.webapp.services.accounts.management.exceptions.single;

public class LoginAttemptsUpdateException extends Exception {
    public static final String EXCEPTION_MESSAGE_UPDATE_LOGIN_ATTEMPTS_FAILED = "Failed to update login attempts for '%s'.";

    public LoginAttemptsUpdateException(final String email) {
        super(String.format(EXCEPTION_MESSAGE_UPDATE_LOGIN_ATTEMPTS_FAILED, email));
    }
}
