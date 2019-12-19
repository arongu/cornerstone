package cornerstone.workflow.app.services.account_service;

import cornerstone.workflow.app.rest.account.AccountDTO;

import java.util.List;

public interface AccountService {
    // Create account
    void createAccount(final String emailAddress, final String password, final boolean available) throws AccountServiceException;
    void createAccounts(final List<AccountDTO> accountDTOS) throws AccountServiceBulkException;

    // Delete account
    void deleteAccount(final String emailAddress) throws AccountServiceException;
    void deleteAccounts(final List<String> emailAddresses) throws AccountServiceBulkException;

    // Change password, email
    void setAccountPassword(final String emailAddress, final String password) throws AccountServiceException;
    void setAccountEmailAddress(final String emailAddress, final String newEmailAddress) throws AccountServiceException;

    // Enable/disable account
    void enableAccount(final String emailAddress) throws AccountServiceException;
    void disableAccount(final String emailAddress, final String reason) throws AccountServiceException;
}
