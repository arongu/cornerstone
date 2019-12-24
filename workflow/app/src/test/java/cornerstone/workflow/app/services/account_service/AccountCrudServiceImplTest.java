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
    public void crud_tests() throws AccountCrudServiceException {
        final String email_address = "crud_tests@x-mail.com";
        final String email_address_changed = "my_new_crud_tests_mail@yahoo.com";
        final String password = "password";
        final String reason = "disable reason";


        final AccountDB accountDB = new AccountDB(configurationProvider);
        final AccountCrudService accountCrudService = new AccountCrudServiceImpl(accountDB);

        // Phase #1 create, delete
        accountCrudService.deleteAccount(email_address);
        accountCrudService.createAccount(email_address, password, true);

        AccountResultSetDto dto = accountCrudService.getAccount(email_address);

        assertTrue(dto.get_account_available());
        assertEquals(email_address, dto.get_email_address());
        assertFalse(dto.get_password_hash().contains(password));


        // Phase #2 change email address
        int id = dto.get_account_id();
        accountCrudService.setAccountEmailAddress(email_address, email_address_changed);
        dto = accountCrudService.getAccount(email_address_changed);

        // Should return null, since email address changed !
        assertNull(accountCrudService.getAccount(email_address));

        // Id should be the same, since the account id is unchanged, only the address
        assertEquals(id, dto.get_account_id());


        // Phase #3 disable account
        accountCrudService.disableAccount(email_address_changed, reason);
        dto = accountCrudService.getAccount(email_address_changed);

        assertFalse(dto.get_account_available());
        assertEquals(reason, dto.get_account_disable_reason());


        // Phase #4 re-enable account
        accountCrudService.enableAccount(email_address_changed);
        dto = accountCrudService.getAccount(email_address_changed);

        assertTrue(dto.get_account_available());


        // Phase #5 cleanup
        accountCrudService.deleteAccount(email_address_changed);
        assertNull(accountCrudService.getAccount(email_address_changed));
    }
}
