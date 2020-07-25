package cornerstone.webapp.services.account.administration;

import cornerstone.webapp.rest.endpoint.account.EmailAndPassword;

import java.util.List;
import java.util.NoSuchElementException;

public interface AccountManagerInterface {
    // Create
    int create(final String emailAddress, final String password, final boolean accountLocked, final boolean verified) throws AccountManagerException;
    int create(final List<EmailAndPassword> list) throws AccountManagerMultipleException;

    // Read
    AccountResultSet get(final String emailAddress) throws AccountManagerException, NoSuchElementException;

    // Update
    int setPassword(final String emailAddress, final String password) throws AccountManagerException;
    int setEmailAddress(final String emailAddress, final String newEmailAddress) throws AccountManagerException;

    int incrementLoginAttempts(final String emailAddress) throws AccountManagerException;
    int clearLoginAttempts(final String emailAddress) throws AccountManagerException;

    int lock(final String emailAddress, final String reason) throws AccountManagerException;
    int unlock(final String emailAddress) throws AccountManagerException;

    // Delete
    int delete(final String emailAddress) throws AccountManagerException;
    int delete(final List<String> emailAddresses) throws AccountManagerMultipleException;

    // Login
    boolean login(final String emailAddress, final String password) throws AccountManagerException;
}
