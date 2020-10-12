package cornerstone.webapp.services.account.management.exceptions.single;

public class BadPasswordException extends Exception {
    public static final String EXCEPTION_MESSAGE_BAD_PASSWORD_PROVIDED = "Bad password provided for '%s'.";

    public BadPasswordException(final String email) {
        super(String.format(EXCEPTION_MESSAGE_BAD_PASSWORD_PROVIDED, email));
    }
}
