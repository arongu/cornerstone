package cornerstone.webapp.rest.endpoints.login;

import cornerstone.webapp.config.ConfigLoader;
import cornerstone.webapp.rest.endpoints.accounts.dtos.AccountEmailPassword;
import cornerstone.webapp.rest.error_responses.ErrorResponse;
import cornerstone.webapp.services.account.management.AccountManager;
import cornerstone.webapp.services.account.management.AccountManagerImpl;
import cornerstone.webapp.services.account.management.AccountResultSet;
import cornerstone.webapp.services.account.management.exceptions.single.BadPasswordException;
import cornerstone.webapp.services.account.management.exceptions.single.LockedException;
import cornerstone.webapp.services.account.management.exceptions.single.NoAccountException;
import cornerstone.webapp.services.account.management.exceptions.single.UnverifiedEmailException;
import cornerstone.webapp.services.jwt.JWTService;
import cornerstone.webapp.services.jwt.JWTServiceImpl;
import cornerstone.webapp.services.keys.rotation.KeyPairWithUUID;
import cornerstone.webapp.services.keys.stores.local.LocalKeyStore;
import cornerstone.webapp.services.keys.stores.local.LocalKeyStoreImpl;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.ws.rs.core.Response;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
/*
    accountEmailPassword          - null [ OK ]
    accountEmailPassword.email    - null [ OK ]
    accountEmailPassword.password - null [ OK ]

 */
public class LoginRestServiceTest {
    @Test
    public void authenticateUser_shouldReturnBadRequest_whenAccountEmailPasswordIsNull() throws Exception {
        final LoginRestService loginRestService = new LoginRestService(null, null);


        final Response response           = loginRestService.authenticateUser(null);
        final ErrorResponse errorResponse = (ErrorResponse) response.getEntity();


        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("Null value provided for email/password.", errorResponse.getError());
    }

    @Test
    public void authenticateUser_shouldReturnUnauthorized_whenEmailIsNull() throws Exception {
        final LoginRestService loginRestService = new LoginRestService(null, null);
        final AccountEmailPassword accountEmailPassword = new AccountEmailPassword(null, "password");


        final Response response           = loginRestService.authenticateUser(accountEmailPassword);
        final ErrorResponse errorResponse = (ErrorResponse) response.getEntity();


        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("Null value provided for email/password.", errorResponse.getError());
    }

    @Test
    public void authenticateUser_shouldReturnUnauthorized_whenPasswordIsNull() throws Exception {
        final LoginRestService loginRestService = new LoginRestService(null, null);
        final AccountEmailPassword accountEmailPassword = new AccountEmailPassword("email", null);


        final Response response           = loginRestService.authenticateUser(accountEmailPassword);
        final ErrorResponse errorResponse = (ErrorResponse) response.getEntity();


        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("Null value provided for email/password.", errorResponse.getError());
    }

    @Test
    public void authenticateUser_shouldReturnUnauthorized_whenAccountManagerThrowBadPasswordException() throws Exception {
        final String email                              = "randomemail@xhost.com";
        final String password                           = "password";
        final AccountManager accountManager             = Mockito.mock(AccountManagerImpl.class);
        final LoginRestService loginRestService         = new LoginRestService(accountManager, null);
        final AccountEmailPassword accountEmailPassword = new AccountEmailPassword(email, password);
        Mockito.when(accountManager.login(Mockito.anyString(), Mockito.anyString())).thenThrow(new BadPasswordException(email));


        final Response response           = loginRestService.authenticateUser(accountEmailPassword);
        final ErrorResponse errorResponse = (ErrorResponse) response.getEntity();


        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
        assertEquals("Unauthorized.", errorResponse.getError());
        Mockito.verify(accountManager, Mockito.times(1)).login(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void authenticateUser_shouldReturnUnauthorized_whenAccountManager_throwsLockedException() throws Exception {
        final String email                              = "mymail@zzz.com";
        final AccountManager accountManager             = Mockito.mock(AccountManagerImpl.class);
        final LoginRestService loginRestService         = new LoginRestService(accountManager, null);
        final AccountEmailPassword accountEmailPassword = new AccountEmailPassword(email, "password");
        final LockedException lockedException           = new LockedException(email);
        Mockito.when(accountManager.login(Mockito.anyString(), Mockito.anyString())).thenThrow(lockedException);


        final Response response           = loginRestService.authenticateUser(accountEmailPassword);
        final ErrorResponse errorResponse = (ErrorResponse) response.getEntity();


        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
        assertEquals("Unauthorized.", errorResponse.getError());
        Mockito.verify(accountManager, Mockito.times(1)).login(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void authenticateUser_shouldReturnUnauthorized_whenAccountManager_throwsUnverifiedEmailExceptionException() throws Exception {
        final String email                                      = "unvf_mymail@zzz.com";
        final AccountManager accountManager                     = Mockito.mock(AccountManagerImpl.class);
        final LoginRestService loginRestService                 = new LoginRestService(accountManager, null);
        final AccountEmailPassword accountEmailPassword         = new AccountEmailPassword(email, "password");
        final UnverifiedEmailException unverifiedEmailException = new UnverifiedEmailException(email);
        Mockito.when(accountManager.login(Mockito.anyString(), Mockito.anyString())).thenThrow(unverifiedEmailException);


        final Response response           = loginRestService.authenticateUser(accountEmailPassword);
        final ErrorResponse errorResponse = (ErrorResponse) response.getEntity();


        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
        assertEquals("Unauthorized.", errorResponse.getError());
        Mockito.verify(accountManager, Mockito.times(1)).login(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void authenticateUser_shouldReturnUnauthorized_whenAccountManager_throwsNoAccountException() throws Exception {
        final String email                              = "bbbb_mymail@zzz.com";
        final AccountManager accountManager             = Mockito.mock(AccountManagerImpl.class);
        final LoginRestService loginRestService         = new LoginRestService(accountManager, null);
        final AccountEmailPassword accountEmailPassword = new AccountEmailPassword(email, "password");
        final NoAccountException noAccountException     = new NoAccountException(email);
        Mockito.when(accountManager.login(Mockito.anyString(), Mockito.anyString())).thenThrow(noAccountException);


        final Response response           = loginRestService.authenticateUser(accountEmailPassword);
        final ErrorResponse errorResponse = (ErrorResponse) response.getEntity();


        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
        assertEquals("Unauthorized.", errorResponse.getError());
        Mockito.verify(accountManager, Mockito.times(1)).login(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void authenticateUser_shouldReturnJWS_whenAccountManagerReturnsTrue() throws Exception {
        final String test_files_dir                     = "../../_test_config/";
        final String keyFile                            = Paths.get(test_files_dir + "key.conf").toAbsolutePath().normalize().toString();
        final String confFile                           = Paths.get(test_files_dir + "app.conf").toAbsolutePath().normalize().toString();
        final ConfigLoader configLoader                 = new ConfigLoader(keyFile, confFile);
        final LocalKeyStore localKeyStore               = new LocalKeyStoreImpl();
        final KeyPairWithUUID keyPairWithUUID           = new KeyPairWithUUID();
        localKeyStore.setupSigning(keyPairWithUUID.uuid, keyPairWithUUID.keyPair.getPrivate(), keyPairWithUUID.keyPair.getPublic());
        final AccountManager accountManager             = Mockito.mock(AccountManagerImpl.class);
        final JWTService jwtService                     = new JWTServiceImpl(configLoader, localKeyStore);
        final LoginRestService loginRestService         = new LoginRestService(accountManager, jwtService);
        final AccountEmailPassword accountEmailPassword = new AccountEmailPassword("cookiemonster@gmail.com", "password");
        final AccountResultSet accountResultSet         = new AccountResultSet(
                1,
                null,
                false,
                null,
                null,
                0,
                "aaaa@mail.com",
                null,
                true,
                null,
                "hash",
                null,
                1,
                "USER"
        );
        Mockito.when(accountManager.login(Mockito.anyString(), Mockito.anyString())).thenReturn(accountResultSet);


        final Response response = loginRestService.authenticateUser(accountEmailPassword);
        final TokenDTO tokenDTO = (TokenDTO) response.getEntity();


        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        Mockito.verify(accountManager, Mockito.times(1)).login(Mockito.anyString(), Mockito.anyString());
    }
}
