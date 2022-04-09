package cornerstone.webapp.services.accounts.management.exceptions.account.single;

public class AccountRetrievalException extends Exception {
    public static final String EXCEPTION_MESSAGE_ACCOUNT_RETRIEVAL_FAILED = "Failed to retrieve '%s'.";

    public AccountRetrievalException(final String mail) {
        super(String.format(EXCEPTION_MESSAGE_ACCOUNT_RETRIEVAL_FAILED, mail));
    }
}
