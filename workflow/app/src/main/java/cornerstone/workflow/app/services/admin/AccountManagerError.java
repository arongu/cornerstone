package cornerstone.workflow.app.services.admin;

public class AccountManagerError {
    public final String email, cause;

    public AccountManagerError(final String email, final String cause) {
        this.email = email;
        this.cause = cause;
    }

    @Override
    public String toString() {
        return "{" +
                "\"email\": \"" + email + "\"," +
                "\"cause\": \"" + cause + "\"}";
    }
}
