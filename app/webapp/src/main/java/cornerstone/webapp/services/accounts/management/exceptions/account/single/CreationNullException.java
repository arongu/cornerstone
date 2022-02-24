package cornerstone.webapp.services.accounts.management.exceptions.account.single;

public class CreationNullException extends Exception {
    public CreationNullException() {
        super("Failed to create account due to missing field(s).");
    }

    public CreationNullException(final String message) {
        super(message);
    }
}
