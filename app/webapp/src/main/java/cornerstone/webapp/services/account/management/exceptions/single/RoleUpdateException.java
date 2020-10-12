package cornerstone.webapp.services.account.management.exceptions.single;

public class RoleUpdateException extends Exception {
    public static final String EXCEPTION_MESSAGE_ACCOUNT_ROLE_UPDATE_FAILED = "Failed to update role for '%s' to '%s'.";

    public RoleUpdateException(final String email, final String roleName) {
        super(String.format(EXCEPTION_MESSAGE_ACCOUNT_ROLE_UPDATE_FAILED, email, roleName));
    }
}
