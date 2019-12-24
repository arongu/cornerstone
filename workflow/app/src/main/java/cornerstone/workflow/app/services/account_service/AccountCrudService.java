package cornerstone.workflow.app.services.account_service;

import cornerstone.workflow.app.rest.account.AccountLoginJsonDto;

import java.util.List;

public interface AccountCrudService {
    // Get account
    AccountResultSetDto getAccount(final String emailAddress) throws AccountCrudServiceException;

    // Create account
    int createAccount(final String emailAddress, final String password, final boolean available) throws AccountCrudServiceException;
    int createAccounts(final List<AccountLoginJsonDto> accountLoginJsonDtos) throws AccountCrudServiceBulkException;

    // Delete account
    int deleteAccount(final String emailAddress) throws AccountCrudServiceException;
    int deleteAccounts(final List<String> emailAddresses) throws AccountCrudServiceBulkException;

    // Change password, email
    int setAccountPassword(final String emailAddress, final String password) throws AccountCrudServiceException;
    int setAccountEmailAddress(final String emailAddress, final String newEmailAddress) throws AccountCrudServiceException;

    // Enable/disable account
    int enableAccount(final String emailAddress) throws AccountCrudServiceException;
    int disableAccount(final String emailAddress, final String reason) throws AccountCrudServiceException;
}
