package cornerstone.webapp.rest.endpoints.accounts;

import cornerstone.webapp.rest.endpoints.accounts.dtos.AccountSearch;
import cornerstone.webapp.rest.error_responses.ErrorResponse;
import cornerstone.webapp.services.account.management.AccountManager;
import cornerstone.webapp.services.account.management.AccountManagerImpl;
import cornerstone.webapp.services.account.management.AccountResultSet;
import cornerstone.webapp.services.account.management.exceptions.single.EmailAddressSearchException;
import cornerstone.webapp.services.account.management.exceptions.single.NoAccountException;
import cornerstone.webapp.services.account.management.exceptions.single.RetrievalException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.ws.rs.core.Response;
import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AccountManagerRestServiceTest {
    @Test
    public void searchAddressShould_shouldReturnEmptyListWith200OK_whenAccountManagerReturnsEmptyList() throws EmailAddressSearchException {
        final AccountManager accountManager                       = Mockito.mock(AccountManagerImpl.class);
        final AccountManagerRestService accountManagerRestService = new AccountManagerRestService(accountManager);
        final AccountSearch accountSearch                         = new AccountSearch();
        accountSearch.setSearchString(null);
        Mockito.when(accountManager.searchAccounts(Mockito.anyString())).thenReturn(new LinkedList<>());


        final Response response       = accountManagerRestService.searchAddress(accountSearch);
        final List<String> resultList = (List<String>) response.getEntity();


        assertEquals(0, resultList.size());
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void searchAddressShould_shouldReturnEmptyListWith200OK_whenAccountManagerReturnsNonEmptyList() throws EmailAddressSearchException {
        final AccountManager accountManager                       = Mockito.mock(AccountManagerImpl.class);
        final AccountManagerRestService accountManagerRestService = new AccountManagerRestService(accountManager);
        final AccountSearch accountSearch                         = new AccountSearch();
        final List<String> list                                   = new LinkedList<>();
        accountSearch.setSearchString("searchString");
        list.add("account_a@mail.com");
        list.add("account_b@mail.com");
        list.add("account_c@mail.com");
        Mockito.when(accountManager.searchAccounts(Mockito.any())).thenReturn(list);


        final Response response       = accountManagerRestService.searchAddress(accountSearch);
        final List<String> resultList = (List<String>) response.getEntity();


        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(3, resultList.size());
        assertEquals(list, resultList);
    }

    @Test
    public void searchAddressShould_shouldReturnErrorResponseWith500_whenAccountManagerThrowsEmailAddressSearchException() throws EmailAddressSearchException {
        final AccountManager accountManager                       = Mockito.mock(AccountManagerImpl.class);
        final AccountManagerRestService accountManagerRestService = new AccountManagerRestService(accountManager);
        final AccountSearch accountSearch                         = new AccountSearch();
        final String exceptionMessage                             = "MyMessage";
        Mockito.when(accountManager.searchAccounts(Mockito.any())).thenThrow(new EmailAddressSearchException(exceptionMessage));


        final Response response           = accountManagerRestService.searchAddress(accountSearch);
        final ErrorResponse errorResponse = (ErrorResponse) response.getEntity();


        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertEquals(exceptionMessage, errorResponse.getError());
    }

    // accountManager.get - result
    @Test
    public void getAccount_shouldReturnAccountResultSetWith200OK_whenThereIsAccount() throws RetrievalException, NoAccountException {
        final AccountManager accountManager                       = Mockito.mock(AccountManagerImpl.class);
        final AccountManagerRestService accountManagerRestService = new AccountManagerRestService(accountManager);
        final int       account_id                                = 111;
        final Timestamp account_registration_ts                   = new Timestamp(System.currentTimeMillis());
        final boolean   account_locked                            = false;
        final Timestamp account_locked_ts                         = null;
        final String    account_lock_reason                       = null;
        final int       account_login_attempts                    = 0;
        final String    email_address                             = "murphy@mail.com";
        final Timestamp email_address_ts                          = account_registration_ts;
        final boolean   email_address_verified                    = true;
        final Timestamp email_address_verified_ts                 = new Timestamp(System.currentTimeMillis() + 1000000);
        final String    password_hash                             = "hash";
        final Timestamp password_hash_ts                          = account_registration_ts;
        final AccountResultSet accountResultSet                   = new AccountResultSet(
                account_id, account_registration_ts,
                account_locked,account_locked_ts,
                account_lock_reason, account_login_attempts,
                email_address, email_address_ts,
                email_address_verified, email_address_verified_ts,
                password_hash, password_hash_ts
        );
        Mockito.when(accountManager.get(Mockito.anyString())).thenReturn(accountResultSet);


        final Response response          = accountManagerRestService.getAccount("string");
        final AccountResultSet resultSet = (AccountResultSet) response.getEntity();


        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(accountResultSet, resultSet);
    }

    // NoAccountException
    @Test
    public void getAccount_shouldReturnErrorResponseWith404NotFound_whenNoAccountExceptionIsThrown() throws RetrievalException, NoAccountException {
        final AccountManager accountManager                       = Mockito.mock(AccountManagerImpl.class);
        final AccountManagerRestService accountManagerRestService = new AccountManagerRestService(accountManager);
        final String exceptionMessage                             = "no such account";
        Mockito.when(accountManager.get(Mockito.anyString())).thenThrow(new NoAccountException(exceptionMessage));


        final Response response           = accountManagerRestService.getAccount("string");
        final ErrorResponse errorResponse = (ErrorResponse) response.getEntity();


        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals(exceptionMessage, errorResponse.getError());
    }

    // RetrievalException
    @Test
    public void getAccount_shouldReturnErrorResponseWith500InternalServerError_whenRetrievalExceptionIsThrown() throws RetrievalException, NoAccountException {
        final AccountManager accountManager                       = Mockito.mock(AccountManagerImpl.class);
        final AccountManagerRestService accountManagerRestService = new AccountManagerRestService(accountManager);
        final String exceptionMessage                             = "exception message";
        Mockito.when(accountManager.get(Mockito.anyString())).thenThrow(new RetrievalException(exceptionMessage));


        final Response response           = accountManagerRestService.getAccount("string");
        final ErrorResponse errorResponse = (ErrorResponse) response.getEntity();


        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertEquals(exceptionMessage, errorResponse.getError());
    }
}
