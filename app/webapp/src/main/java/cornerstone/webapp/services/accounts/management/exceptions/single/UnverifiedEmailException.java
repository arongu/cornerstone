package cornerstone.webapp.services.accounts.management.exceptions.single;

public class UnverifiedEmailException extends Exception {
    public static final String EXCEPTION_MESSAGE_ACCOUNT_NOT_VERIFIED = "Account is not verified '%s'.";

    public UnverifiedEmailException(final String email) {
        super(String.format(EXCEPTION_MESSAGE_ACCOUNT_NOT_VERIFIED, email));
    }
}
