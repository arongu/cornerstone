package cornerstone.workflow.app.services.account_service;

import cornerstone.workflow.app.rest.endpoints.account.AccountDTO;

import java.util.List;

public interface AccountService {
    void createAccount(final String emailAddress, final String password) throws AccountServiceException;
    void createAccounts(final List<AccountDTO> accountDTOS) throws AccountServiceBulkException;
    void deleteAccount(final String emailAddress) throws AccountServiceException;
    void deleteAccounts(final List<String> emailAddresses) throws AccountServiceBulkException;

    // TODO
    //    boolean changeAccountPassword(final String email, final String password);
    //    boolean changeAccountEmail(final String email);
    //    enable/disableAccount
}
