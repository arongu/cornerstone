package cornerstone.webapp.services.account.administration;

import cornerstone.webapp.rest.endpoint.account.EmailAndPassword;

import java.util.List;

public interface AccountAdministrationInterface {
    // Create
    int create(final String emailAddress, final String password, final boolean accountLocked, final boolean verified) throws AccountAdministrationException;
    int create(final List<EmailAndPassword> list) throws AccountAdministrationMultipleException;

    // Read
    AccountResultSet get(final String emailAddress) throws AccountAdministrationException;

    // Update
    int setPassword(final String emailAddress, final String password) throws AccountAdministrationException;
    int setEmailAddress(final String emailAddress, final String newEmailAddress) throws AccountAdministrationException;

    int incrementLoginAttempts(final String emailAddress) throws AccountAdministrationException;
    int clearLoginAttempts(final String emailAddress) throws AccountAdministrationException;

    int lock(final String emailAddress, final String reason) throws AccountAdministrationException;
    int unlock(final String emailAddress) throws AccountAdministrationException;

    // Delete
    int delete(final String emailAddress) throws AccountAdministrationException;
    int delete(final List<String> emailAddresses) throws AccountAdministrationMultipleException;

    // Login
    boolean login(final String emailAddress, final String password) throws AccountAdministrationException;
}
