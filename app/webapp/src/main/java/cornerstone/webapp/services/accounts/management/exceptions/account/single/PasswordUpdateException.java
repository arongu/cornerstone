package cornerstone.webapp.services.accounts.management.exceptions.account.single;

public class PasswordUpdateException extends Exception {
    public static final String EXCEPTION_MESSAGE_UPDATE_PASSWORD_FAILED = "Failed to update password for '%s'.";

    public PasswordUpdateException(final String email) {
        super(String.format(EXCEPTION_MESSAGE_UPDATE_PASSWORD_FAILED, email));
    }
}
