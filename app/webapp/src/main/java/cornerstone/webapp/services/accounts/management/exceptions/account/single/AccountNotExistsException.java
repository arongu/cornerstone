package cornerstone.webapp.services.accounts.management.exceptions.account.single;

public class AccountNotExistsException extends Exception {
    public static final String EXCEPTION_MESSAGE_ACCOUNT_DOES_NOT_EXIST = "Account '%s' does not exist.";

    public AccountNotExistsException(final String account) {
        super(String.format(EXCEPTION_MESSAGE_ACCOUNT_DOES_NOT_EXIST, account));
    }
}
