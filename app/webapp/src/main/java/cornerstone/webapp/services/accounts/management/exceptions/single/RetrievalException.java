package cornerstone.webapp.services.accounts.management.exceptions.single;

public class RetrievalException extends Exception {
    public static final String EXCEPTION_MESSAGE_ACCOUNT_RETRIEVAL_FAILED = "Failed to retrieve '%s'.";

    public RetrievalException(final String mail) {
        super(String.format(EXCEPTION_MESSAGE_ACCOUNT_RETRIEVAL_FAILED, mail));
    }
}
