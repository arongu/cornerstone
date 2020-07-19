package cornerstone.webapp.services.account_service;

import cornerstone.webapp.rest.endpoint.account.EmailAndPassword;

import java.util.List;

public interface AccountServiceInterface {
    // Create
    int create(final String emailAddress, final String password, final boolean accountLocked, final boolean verified) throws AccountServiceException;
    int create(final List<EmailAndPassword> list) throws AccountServiceMultipleException;

    // Read
    AccountResultSet get(final String emailAddress) throws AccountServiceException;

    // Update
    int setPassword(final String emailAddress, final String password) throws AccountServiceException;
    int setEmailAddress(final String emailAddress, final String newEmailAddress) throws AccountServiceException;

    int incrementLoginAttempts(final String emailAddress) throws AccountServiceException;
    int clearLoginAttempts(final String emailAddress) throws AccountServiceException;

    int lock(final String emailAddress, final String reason) throws AccountServiceException;
    int unlock(final String emailAddress) throws AccountServiceException;

    // Delete
    int delete(final String emailAddress) throws AccountServiceException;
    int delete(final List<String> emailAddresses) throws AccountServiceMultipleException;

    // Login
    boolean login(final String emailAddress, final String password) throws AccountServiceException;
}
