package cornerstone.workflow.app.services.account_service;

import cornerstone.workflow.app.configuration.ConfigurationProvider;
import cornerstone.workflow.app.datasource.AccountDB;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class AccountServiceImplTest {
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
    public void getAccount_shouldReturnNull_whenEmailDoesNotExist() throws AccountServiceException {
        final AccountDB accountDB = new AccountDB(configurationProvider);
        final AccountService accountService = new AccountServiceImpl(accountDB);

        assertNull(accountService.getAccount("nosuch@mail.com"));
    }

    @Test
    public void crud_tests() throws AccountServiceException {
        final String email_address = "crud_tests@x-mail.com";
        final String email_address_changed = "my_new_crud_tests_mail@yahoo.com";
        final String password = "password";
        final String reason = "disable reason";


        final AccountDB accountDB = new AccountDB(configurationProvider);
        final AccountService accountService = new AccountServiceImpl(accountDB);

        // Phase #1 create, delete
        accountService.createAccount(email_address, password, true);    // create account

        AccountResultSetDto dto = accountService.getAccount(email_address);

        assertTrue(dto.get_account_locked());                    // should return true
        assertEquals(email_address, dto.get_email_address());       // should match with the specified email
        assertFalse(dto.get_password_hash().contains(password));    // should not contain password string


        // Phase #2 change email address
        int id = dto.get_account_id();
        accountService.setAccountEmailAddress(email_address, email_address_changed);    // change email address
        dto = accountService.getAccount(email_address_changed);                         // get account

        // Should return null, since email address changed !
        assertNull(accountService.getAccount(email_address));                           // should return null

        // Id should be the same, since the account id is unchanged, only the address
        assertEquals(id, dto.get_account_id());


        // Phase #3 disable account
        accountService.unlockAccount(email_address_changed, reason);                   // disable account
        dto = accountService.getAccount(email_address_changed);                         // get account

        assertFalse(dto.get_account_locked());                                           // should return false
        assertEquals(reason, dto.get_account_disable_reason());                             // should match with reason


        // Phase #4 re-enable account
        accountService.lockAccount(email_address_changed);                            // enable account
        dto = accountService.getAccount(email_address_changed);                         // get account

        assertTrue(dto.get_account_locked());                                            // should be true


        // Phase #5 password change
        String originalPassword = dto.get_password_hash();                                  // store original password hash
        accountService.setAccountPassword(email_address_changed, "newpassword");
        dto = accountService.getAccount(email_address_changed);


        assertNotEquals(originalPassword, dto.get_password_hash());


        // Phase #6 cleanup
        accountService.deleteAccount(email_address_changed);                            // delete account
        assertNull(accountService.getAccount(email_address_changed));                   // cleanup and delete test
    }
}
