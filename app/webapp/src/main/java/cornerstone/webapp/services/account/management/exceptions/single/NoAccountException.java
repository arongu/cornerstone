package cornerstone.webapp.services.account.management.exceptions.single;

public class NoAccountException extends Exception {
    public static final String EXCEPTION_MESSAGE_ACCOUNT_DOES_NOT_EXIST = "Account '%s' does not exist.";

    public NoAccountException(final String account) {
        super(String.format(EXCEPTION_MESSAGE_ACCOUNT_DOES_NOT_EXIST, account));
    }
}
