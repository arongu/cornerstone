package cornerstone.workflow.app.services.account_service;

import cornerstone.workflow.app.rest.endpoint.account.EmailAndPassword;

import java.util.List;

public interface AccountService {
    // Get account
    AccountResultSetDto getAccount(final String emailAddress) throws AccountServiceException;

    // CRUD
    int createAccount(final String emailAddress, final String password, final boolean accountLocked) throws AccountServiceException;
    int createAccounts(final List<EmailAndPassword> list) throws AccountServiceBulkException;

    int deleteAccount(final String emailAddress) throws AccountServiceException;
    int deleteAccounts(final List<String> emailAddresses) throws AccountServiceBulkException;

    // Change password, email
    int setAccountPassword(final String emailAddress, final String password) throws AccountServiceException;
    int setAccountEmailAddress(final String emailAddress, final String newEmailAddress) throws AccountServiceException;

    int incrementFailedLoginAttempts(final String emailAddress) throws AccountServiceException;
    int clearFailedLoginAttempts(final String emailAddress) throws AccountServiceException;

    // Lock/unlock account
    int lockAccount(final String emailAddress, final String reason) throws AccountServiceException;
    int unlockAccount(final String emailAddress) throws AccountServiceException;

    boolean login(final String emailAddress, final String password) throws AccountServiceException;
}
