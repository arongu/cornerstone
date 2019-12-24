package cornerstone.workflow.app.services.account_service;

import cornerstone.workflow.app.configuration.ConfigurationProvider;
import cornerstone.workflow.app.datasource.AccountDB;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class AccountCrudServiceImplTest {
    private static final String dev_files_dir = "../../_dev_files/test_config/";
    private static final String confPath  = Paths.get(dev_files_dir + "app.conf").toAbsolutePath().normalize().toString();
    private static final String keyPath = Paths.get(dev_files_dir + "key.conf").toAbsolutePath().normalize().toString();

    private static ConfigurationProvider configurationProvider;

    @BeforeAll
    public static void init() {
        System.setProperty(ConfigurationProvider.SYSTEM_PROPERTY_CONF_FILE, confPath);
        System.setProperty(ConfigurationProvider.SYSTEM_PROPERTY_KEY_FILE, keyPath);

        try {
            configurationProvider = new ConfigurationProvider();
            configurationProvider.loadConfig();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    @AfterAll
    public static void cleanUp() {
        System.clearProperty(ConfigurationProvider.SYSTEM_PROPERTY_CONF_FILE);
        System.clearProperty(ConfigurationProvider.SYSTEM_PROPERTY_KEY_FILE);
    }

    @Test
    public void getAccount_shouldReturnNull_whenEmailDoesNotExist() throws AccountCrudServiceException {
        final AccountDB accountDB = new AccountDB(configurationProvider);
        final AccountCrudService accountCrudService = new AccountCrudServiceImpl(accountDB);

        assertNull(accountCrudService.getAccount("nosuch@mail.com"));
    }

    @Test
    public void getAccount_shouldReturnAccountDto_whenEmailExists() throws AccountCrudServiceException {
        final String EMAIL = "test@mail.com";
        final String PASSWORD = "password";
        final boolean AVAILABLE = true;

        // Phase #1
        final AccountDB accountDB = new AccountDB(configurationProvider);
        final AccountCrudService accountCrudService = new AccountCrudServiceImpl(accountDB);
        accountCrudService.deleteAccount(EMAIL);
        accountCrudService.createAccount(EMAIL, PASSWORD, AVAILABLE);


        AccountResultSetDto result = accountCrudService.getAccount(EMAIL);


        assertEquals(AVAILABLE, result.get_account_available());
        assertEquals(EMAIL, result.get_email_address());
        assertFalse(result.get_password_hash().contains(PASSWORD));


        // Phase #2
        accountCrudService.deleteAccount(EMAIL);
        assertNull(accountCrudService.getAccount(EMAIL));
    }
}
