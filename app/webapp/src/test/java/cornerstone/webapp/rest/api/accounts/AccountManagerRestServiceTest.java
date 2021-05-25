package cornerstone.webapp.rest.api.accounts;

import cornerstone.webapp.rest.api.accounts.dtos.AccountEmailPassword;
import cornerstone.webapp.rest.api.accounts.dtos.AccountSearch;
import cornerstone.webapp.rest.api.accounts.dtos.AccountSetup;
import cornerstone.webapp.rest.error_responses.ErrorResponse;
import cornerstone.webapp.rest.error_responses.MultiErrorResponse;
import cornerstone.webapp.services.accounts.management.AccountManager;
import cornerstone.webapp.services.accounts.management.AccountManagerImpl;
import cornerstone.webapp.services.accounts.management.AccountResultSet;
import cornerstone.webapp.services.accounts.management.UserRole;
import cornerstone.webapp.services.accounts.management.exceptions.multi.MultiCreationException;
import cornerstone.webapp.services.accounts.management.exceptions.multi.MultiCreationInitialException;
import cornerstone.webapp.services.accounts.management.exceptions.multi.MultiDeletionException;
import cornerstone.webapp.services.accounts.management.exceptions.multi.MultiDeletionInitialException;
import cornerstone.webapp.services.accounts.management.exceptions.single.*;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.ws.rs.core.Response;
import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AccountManagerRestServiceTest {
    @Test
    public void searchAddressShould_shouldReturnEmptyListWith200OK_whenAccountManagerReturnsEmptyList() throws AccountSearchException {
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
    public void searchAddressShould_shouldReturnEmptyListWith200OK_whenAccountManagerReturnsNonEmptyList() throws AccountSearchException {
        final AccountManager            accountManager            = Mockito.mock(AccountManagerImpl.class);
        final AccountManagerRestService accountManagerRestService = new AccountManagerRestService(accountManager);
        final AccountSearch             accountSearch             = new AccountSearch();
        final List<String>              list                      = new LinkedList<>();
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
    public void searchAddress_shouldReturnErrorResponseWith503_whenAccountManagerThrowsEmailAddressSearchException() throws AccountSearchException {
        final AccountManager            accountManager            = Mockito.mock(AccountManagerImpl.class);
        final AccountManagerRestService accountManagerRestService = new AccountManagerRestService(accountManager);
        final AccountSearch             accountSearch             = new AccountSearch();
        final String                    keyword                   = "keyword%";
        Mockito.when(accountManager.searchAccounts(Mockito.any())).thenThrow(new AccountSearchException(keyword));


        final Response response = accountManagerRestService.searchAddress(accountSearch);
        final ErrorResponse er  = (ErrorResponse) response.getEntity();


        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertEquals("Failed to run search with keyword 'keyword%'.", er.getError());
    }

    // result
    @Test
    public void getAccount_shouldReturnAccountResultSetWith200OK_whenThereIsAccount() throws RetrievalException, NoAccountException {
        final AccountManager            accountManager            = Mockito.mock(AccountManagerImpl.class);
        final AccountManagerRestService accountManagerRestService = new AccountManagerRestService(accountManager);
        final int                       account_id                = 111;
        final Timestamp                 account_registration_ts   = new Timestamp(System.currentTimeMillis());
        final boolean                   account_locked            = false;
        final Timestamp                 account_locked_ts         = null;
        final String                    account_lock_reason       = null;
        final int                       account_login_attempts    = 0;
        final String                    email_address             = "murphy@mail.com";
        final Timestamp                 email_address_ts          = account_registration_ts;
        final boolean                   email_address_verified    = true;
        final Timestamp                 email_address_verified_ts = new Timestamp(System.currentTimeMillis() + 1000000);
        final String                    password_hash             = "hash";
        final Timestamp                 password_hash_ts          = account_registration_ts;
        final UserRole accountRole               = UserRole.USER;
        final AccountResultSet          accountResultSet          = new AccountResultSet(account_id, account_registration_ts,
                                                                                         account_locked, account_locked_ts,
                                                                                         account_lock_reason, account_login_attempts,
                                                                                         email_address, email_address_ts,
                                                                                         email_address_verified, email_address_verified_ts,
                                                                                         password_hash, password_hash_ts,
                                                                                         accountRole.getId(),
                                                                                         accountRole.name());

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
        final String email                                        = "thereisnoway_this_account_exists@wahahamail.com";
        Mockito.when(accountManager.get(Mockito.anyString())).thenThrow(new NoAccountException(email));


        final Response response = accountManagerRestService.getAccount(email);
        final ErrorResponse er  = (ErrorResponse) response.getEntity();


        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals("Account 'thereisnoway_this_account_exists@wahahamail.com' does not exist.", er.getError());
    }

    // RetrievalException
    @Test
    public void getAccount_shouldReturnErrorResponseWith503InternalServerError_whenRetrievalExceptionIsThrown() throws RetrievalException, NoAccountException {
        final AccountManager accountManager                       = Mockito.mock(AccountManagerImpl.class);
        final AccountManagerRestService accountManagerRestService = new AccountManagerRestService(accountManager);
        final String mail                                         = "funnyxxxxxxxx@mail.com";
        Mockito.when(accountManager.get(Mockito.anyString())).thenThrow(new RetrievalException(mail));


        final Response response           = accountManagerRestService.getAccount(mail);
        final ErrorResponse errorResponse = (ErrorResponse) response.getEntity();


        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertEquals("Failed to retrieve 'funnyxxxxxxxx@mail.com'.", errorResponse.getError());
    }

    @Test
    public void createShouldRespondWith201CreatedAndLocationHeader_whenAccountCreationIsSuccessful() throws CreationException, CreationDuplicateException, CreationNullException {
        final AccountManager accountManager                       = Mockito.mock(AccountManagerImpl.class);
        final AccountManagerRestService accountManagerRestService = new AccountManagerRestService(accountManager);
        final AccountEmailPassword accountEmailPassword           = new AccountEmailPassword("user@gmail.com", "password");
        Mockito.when(accountManager.create(Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyBoolean(), Mockito.any(UserRole.class))).thenReturn(1);


        final Response response = accountManagerRestService.create(accountEmailPassword);


        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        assertEquals("/accounts/user@gmail.com", response.getHeaderString("Location"));
    }

    @Test
    public void createShouldRespondWithErrorResponseWith503InternalServerError_whenCreationExceptionIsThrown() throws CreationException, CreationDuplicateException, CreationNullException {
        final String mail                   = "user@gmail.com";
        final String password               = "password";
        final AccountManager accountManager = Mockito.mock(AccountManagerImpl.class);
        final AccountManagerRestService accountManagerRestService = new AccountManagerRestService(accountManager);
        final AccountEmailPassword accountEmailPassword           = new AccountEmailPassword(mail, password);
        Mockito.when(accountManager.create(Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyBoolean(), Mockito.any(UserRole.class))).thenThrow(new CreationException(mail));


        final Response response = accountManagerRestService.create(accountEmailPassword);
        final ErrorResponse er  = (ErrorResponse) response.getEntity();


        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertEquals("Failed to create 'user@gmail.com'.", er.getError());
    }

    // delete(String)
    // NO CONTENT
    // NOT FOUND
    // INTERNAL
    @Test
    public void deleteShouldRespondWith204NoContent_whenDeleteIsSuccessful() throws DeletionException, NoAccountException {
        final AccountManager accountManager                       = Mockito.mock(AccountManagerImpl.class);
        final AccountManagerRestService accountManagerRestService = new AccountManagerRestService(accountManager);
        Mockito.when(accountManager.delete(Mockito.anyString())).thenReturn(1);


        final Response response = accountManagerRestService.delete("delete_me@mail.com");


        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
    }

    @Test
    public void deleteShouldRespondWithErrorResponseWith404NotFound_whenNoAccountExceptionIsThrown() throws DeletionException, NoAccountException {
        final String email                                        = "delete_me@mail.com";
        final AccountManager accountManager                       = Mockito.mock(AccountManagerImpl.class);
        final AccountManagerRestService accountManagerRestService = new AccountManagerRestService(accountManager);
        Mockito.when(accountManager.delete(Mockito.anyString())).thenThrow(new NoAccountException(email));


        final Response response = accountManagerRestService.delete(email);
        final ErrorResponse er  = (ErrorResponse) response.getEntity();


        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals("Account 'delete_me@mail.com' does not exist.", er.getError());
    }

    @Test
    public void deleteShouldRespondWithErrorResponseWith503InternalServerError_whenDeletionExceptionIsThrown() throws DeletionException, NoAccountException {
        final String email                                        = "delete_me2@mail.com";
        final AccountManager accountManager                       = Mockito.mock(AccountManagerImpl.class);
        final AccountManagerRestService accountManagerRestService = new AccountManagerRestService(accountManager);
        Mockito.when(accountManager.delete(Mockito.anyString())).thenThrow(new DeletionException(email));


        final Response response = accountManagerRestService.delete(email);
        final ErrorResponse er  = (ErrorResponse) response.getEntity();


        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertEquals("Failed to delete 'delete_me2@mail.com'.", er.getError());
    }

    // massCreate
    @Test
    public void massCreate_shouldReturn201Created_whenMassAccountCreationIsSuccessful() throws MultiCreationInitialException, MultiCreationException {
        final AccountManager accountManager                       = Mockito.mock(AccountManagerImpl.class);
        final AccountManagerRestService accountManagerRestService = new AccountManagerRestService(accountManager);
        Mockito.when(accountManager.create(Mockito.anyList())).thenReturn(15);


        final Response response = accountManagerRestService.massCreate(new LinkedList<AccountSetup>());


        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
    }

    @Test
    public void massCreate_shouldRespondWithErrorResponse503InternalServerError_whenMultiCreationInitialExceptionIsThrown() throws MultiCreationInitialException, MultiCreationException {
        final AccountManager accountManager                       = Mockito.mock(AccountManagerImpl.class);
        final AccountManagerRestService accountManagerRestService = new AccountManagerRestService(accountManager);
        Mockito.when(accountManager.create(Mockito.anyList())).thenThrow(MultiCreationInitialException.class);


        final Response response = accountManagerRestService.massCreate(new LinkedList<AccountSetup>());
        final ErrorResponse er  = (ErrorResponse) response.getEntity();


        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertEquals("Database error.", er.getError());
    }

    @Test
    public void massCreate_shouldRespondWithMultiErrorResponse503InternalServerError_whenMultiCreationExceptionIsThrown() throws MultiCreationInitialException, MultiCreationException {
        final AccountManager accountManager                       = Mockito.mock(AccountManagerImpl.class);
        final AccountManagerRestService accountManagerRestService = new AccountManagerRestService(accountManager);
        final String exceptionMessage1                            = String.format(CreationException.EXCEPTION_MESSAGE_ACCOUNT_CREATION_FAILED, "failed_a@mail.com");
        final String exceptionMessage2                            = String.format(CreationException.EXCEPTION_MESSAGE_ACCOUNT_CREATION_FAILED, "boci@mail.com");
        final String exceptionMessage3                            = String.format(CreationException.EXCEPTION_MESSAGE_ACCOUNT_CREATION_FAILED, "lamp@mail.com");
        final MultiCreationException multiCreationException       = new MultiCreationException();
        multiCreationException.addExceptionMessage(exceptionMessage1);
        multiCreationException.addExceptionMessage(exceptionMessage2);
        multiCreationException.addExceptionMessage(exceptionMessage3);
        Mockito.when(accountManager.create(Mockito.anyList())).thenThrow(multiCreationException);


        final Response response     = accountManagerRestService.massCreate(new LinkedList<AccountSetup>());
        final MultiErrorResponse er = (MultiErrorResponse) response.getEntity();


        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertEquals(multiCreationException.getExceptionMessages(), er.getErrors());
    }

    // massDelete
    @Test
    public void massDelete_shouldReturn204NoContent_whenMassAccountDeletionIsSuccessful() throws MultiDeletionInitialException, MultiDeletionException {
        final AccountManager accountManager                       = Mockito.mock(AccountManagerImpl.class);
        final AccountManagerRestService accountManagerRestService = new AccountManagerRestService(accountManager);
        Mockito.when(accountManager.delete(Mockito.anyList())).thenReturn(15);


        final Response response = accountManagerRestService.massDelete(new LinkedList<String>());


        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
    }

    @Test
    public void massDelete_shouldRespondWithErrorResponse503InternalServerError_whenMultiDeletionInitialExceptionIsThrown() throws MultiDeletionInitialException, MultiDeletionException {
        final AccountManager accountManager                       = Mockito.mock(AccountManagerImpl.class);
        final AccountManagerRestService accountManagerRestService = new AccountManagerRestService(accountManager);
        Mockito.when(accountManager.delete(Mockito.anyList())).thenThrow(MultiDeletionInitialException.class);


        final Response response = accountManagerRestService.massDelete(new LinkedList<String>());
        final ErrorResponse er  = (ErrorResponse) response.getEntity();


        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertEquals("Database error.", er.getError());
    }

    @Test
    public void massDelete_shouldRespondWithErrorResponse503InternalServerError_whenMultiDeletionExceptionIsThrown() throws MultiDeletionInitialException, MultiDeletionException {
        final AccountManager accountManager                       = Mockito.mock(AccountManagerImpl.class);
        final AccountManagerRestService accountManagerRestService = new AccountManagerRestService(accountManager);
        final String exceptionMessage1                            = String.format(DeletionException.EXCEPTION_MESSAGE_ACCOUNT_DELETION_FAILED, "failed_a@mail.com");
        final String exceptionMessage2                            = String.format(DeletionException.EXCEPTION_MESSAGE_ACCOUNT_DELETION_FAILED, "boci@mail.com");
        final String exceptionMessage3                            = String.format(DeletionException.EXCEPTION_MESSAGE_ACCOUNT_DELETION_FAILED, "lamp@mail.com");
        final MultiDeletionException multiDeletionException       = new MultiDeletionException();
        multiDeletionException.addExceptionMessage(exceptionMessage1);
        multiDeletionException.addExceptionMessage(exceptionMessage2);
        multiDeletionException.addExceptionMessage(exceptionMessage3);
        Mockito.when(accountManager.delete(Mockito.anyList())).thenThrow(multiDeletionException);


        final Response response     = accountManagerRestService.massDelete(new LinkedList<String>());
        final MultiErrorResponse er = (MultiErrorResponse) response.getEntity();


        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertEquals(multiDeletionException.getExceptionMessages(), er.getErrors());
    }
}
