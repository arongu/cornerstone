package cornerstone.webapp.services.accounts.management.exceptions.single;

public class EmailUpdateException extends Exception {
    public static final String EXCEPTION_MESSAGE_UPDATE_EMAIL_FAILED = "Failed to update email for '%s' -> '%s'.";

    public EmailUpdateException(final String email, final String newEmail) {
        super(String.format(EXCEPTION_MESSAGE_UPDATE_EMAIL_FAILED, email, newEmail));
    }
}
