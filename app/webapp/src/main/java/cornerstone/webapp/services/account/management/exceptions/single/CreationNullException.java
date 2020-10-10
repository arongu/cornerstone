package cornerstone.webapp.services.account.management.exceptions.single;

public class CreationNullException extends Exception {
    public CreationNullException() {
        super("Failed to create account due to missing field(s).");
    }
}
