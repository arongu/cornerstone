package cornerstone.workflow.app.services.account_service;

import cornerstone.workflow.app.rest.account.AccountDTO;

import java.util.List;

public interface AccountService {

    // Create account
    void createAccount(final String emailAddress, final String password) throws AccountServiceException;
    void createAccounts(final List<AccountDTO> accountDTOS) throws AccountServiceBulkException;

    // Delete account
    void deleteAccount(final String emailAddress) throws AccountServiceException;
    void deleteAccounts(final List<String> emailAddresses) throws AccountServiceBulkException;

    // Enable/disable, change password, email
    void setAccountPassword(final String emailAddress, final String password) throws AccountServiceException;
    void setAccountEmailAddress(final String emailAddress, final String newEmailAddress) throws AccountServiceException;
    void setAccountEnabled(final String emailAddress, final boolean enabled) throws AccountServiceException;
}
