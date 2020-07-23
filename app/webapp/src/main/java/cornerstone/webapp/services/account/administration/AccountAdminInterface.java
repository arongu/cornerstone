package cornerstone.webapp.services.account.administration;

import cornerstone.webapp.rest.endpoint.account.EmailAndPassword;

import java.util.List;

public interface AccountAdminInterface {
    // Create
    int create(final String emailAddress, final String password, final boolean accountLocked, final boolean verified) throws AccountAdminException;
    int create(final List<EmailAndPassword> list) throws AccountAdminMultipleException;

    // Read
    AccountResultSet get(final String emailAddress) throws AccountAdminException;

    // Update
    int setPassword(final String emailAddress, final String password) throws AccountAdminException;
    int setEmailAddress(final String emailAddress, final String newEmailAddress) throws AccountAdminException;

    int incrementLoginAttempts(final String emailAddress) throws AccountAdminException;
    int clearLoginAttempts(final String emailAddress) throws AccountAdminException;

    int lock(final String emailAddress, final String reason) throws AccountAdminException;
    int unlock(final String emailAddress) throws AccountAdminException;

    // Delete
    int delete(final String emailAddress) throws AccountAdminException;
    int delete(final List<String> emailAddresses) throws AccountAdminMultipleException;

    // Login
    boolean login(final String emailAddress, final String password) throws AccountAdminException;
}
