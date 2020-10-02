package cornerstone.webapp.rest.endpoints.login;

import cornerstone.webapp.config.ConfigLoader;
import cornerstone.webapp.rest.endpoints.accounts.dtos.AccountEmailPassword;
import cornerstone.webapp.rest.error_responses.ErrorResponse;
import cornerstone.webapp.services.account.management.AccountManager;
import cornerstone.webapp.services.account.management.AccountManagerImpl;
import cornerstone.webapp.services.jwt.JWTService;
import cornerstone.webapp.services.jwt.JWTServiceImpl;
import cornerstone.webapp.services.rsa.rotation.KeyPairWithUUID;
import cornerstone.webapp.services.rsa.store.local.LocalKeyStore;
import cornerstone.webapp.services.rsa.store.local.LocalKeyStoreImpl;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.ws.rs.core.Response;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LoginRestServiceTest {
    @Test
    public void authenticateUser_shouldReturnUnauthorized_whenAccountEmailPasswordIsNull() throws Exception {
        final LoginRestService loginRestService = new LoginRestService(null, null);


        final Response response           = loginRestService.authenticateUser(null);
        final ErrorResponse errorResponse = (ErrorResponse) response.getEntity();


        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
        assertEquals("Unauthorized.", errorResponse.getError());
    }

    @Test
    public void authenticateUser_shouldReturnUnauthorized_whenEmailIsNull() throws Exception {
        final LoginRestService loginRestService = new LoginRestService(null, null);
        final AccountEmailPassword accountEmailPassword = new AccountEmailPassword(null, "password");


        final Response response           = loginRestService.authenticateUser(accountEmailPassword);
        final ErrorResponse errorResponse = (ErrorResponse) response.getEntity();


        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
        assertEquals("Unauthorized.", errorResponse.getError());
    }

    @Test
    public void authenticateUser_shouldReturnUnauthorized_whenPasswordIsNull() throws Exception {
        final LoginRestService loginRestService = new LoginRestService(null, null);
        final AccountEmailPassword accountEmailPassword = new AccountEmailPassword("email", null);


        final Response response           = loginRestService.authenticateUser(accountEmailPassword);
        final ErrorResponse errorResponse = (ErrorResponse) response.getEntity();


        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
        assertEquals("Unauthorized.", errorResponse.getError());
    }

    @Test
    public void authenticateUser_shouldReturnUnauthorized_whenAccountManagerReturnsFalse() throws Exception {
        final AccountManager accountManager             = Mockito.mock(AccountManagerImpl.class);
        final LoginRestService loginRestService         = new LoginRestService(accountManager, null);
        final AccountEmailPassword accountEmailPassword = new AccountEmailPassword("email", "password");
        Mockito.when(accountManager.login(Mockito.anyString(), Mockito.anyString())).thenReturn(false);


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
        final AccountEmailPassword accountEmailPassword = new AccountEmailPassword("email", "password");
        Mockito.when(accountManager.login(Mockito.anyString(), Mockito.anyString())).thenReturn(true);


        final Response response = loginRestService.authenticateUser(accountEmailPassword);
        final TokenDTO tokenDTO = (TokenDTO) response.getEntity();
        System.out.println("token: " + tokenDTO.getToken());


        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        Mockito.verify(accountManager, Mockito.times(1)).login(Mockito.anyString(), Mockito.anyString());
    }
}
