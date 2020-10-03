package cornerstone.webapp.services.account.management.exceptions.single;

public class AccountSearchException extends Exception {
    public static final String EXCEPTION_MESSAGE_ACCOUNT_SEARCH_FAILED = "Failed to run search with keyword '%s'.";

    public AccountSearchException(final String keyword) {
        super(String.format(EXCEPTION_MESSAGE_ACCOUNT_SEARCH_FAILED, keyword));
    }
}
