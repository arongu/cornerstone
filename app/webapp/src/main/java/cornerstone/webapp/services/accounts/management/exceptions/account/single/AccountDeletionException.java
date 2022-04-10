package cornerstone.webapp.services.accounts.management.exceptions.account.single;

public class AccountDeletionException extends Exception {
    public static final String EXCEPTION_MESSAGE_ACCOUNT_DELETION_FAILED = "Failed to delete '%s'.";

    public AccountDeletionException(final String account) {
        super(String.format(EXCEPTION_MESSAGE_ACCOUNT_DELETION_FAILED, account));
    }
}
