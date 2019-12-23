package cornerstone.workflow.app.services.account_service;

import cornerstone.workflow.app.rest.account.AccountLoginJsonDto;

import java.util.List;

public interface AccountCrudService {
    // Get account
    AccountResultSetDto getAccount(final String emailAddress) throws AccountCrudServiceException;

    // Create account
    void createAccount(final String emailAddress, final String password, final boolean available) throws AccountCrudServiceException;
    void createAccounts(final List<AccountLoginJsonDto> accountLoginJsonDtos) throws AccountCrudServiceBulkException;

    // Delete account
    void deleteAccount(final String emailAddress) throws AccountCrudServiceException;
    void deleteAccounts(final List<String> emailAddresses) throws AccountCrudServiceBulkException;

    // Change password, email
    void setAccountPassword(final String emailAddress, final String password) throws AccountCrudServiceException;
    void setAccountEmailAddress(final String emailAddress, final String newEmailAddress) throws AccountCrudServiceException;

    // Enable/disable account
    void enableAccount(final String emailAddress) throws AccountCrudServiceException;
    void disableAccount(final String emailAddress, final String reason) throws AccountCrudServiceException;
}
