package cornerstone.workflow.app.services.account_service;

import cornerstone.workflow.app.configuration.ConfigurationProvider;
import cornerstone.workflow.app.datasource.DataSourceAccountDB;
import org.apache.commons.codec.digest.Crypt;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AccountServiceImplTest {
    private static AccountService service;

    @BeforeAll
    public static void setSystemProperties() {
        final String dev_files_dir = "../../_dev_files/test_config/";
        final String confPath = Paths.get(dev_files_dir + "app.conf").toAbsolutePath().normalize().toString();
        final String keyPath = Paths.get(dev_files_dir + "key.conf").toAbsolutePath().normalize().toString();

        System.setProperty(ConfigurationProvider.SYSTEM_PROPERTY_KEY__CONF_FILE, confPath);
        System.setProperty(ConfigurationProvider.SYSTEM_PROPERTY_KEY__KEY_FILE, keyPath);

        try {
            final ConfigurationProvider cfp = new ConfigurationProvider();
            cfp.loadConfig();

            final DataSourceAccountDB ds = new DataSourceAccountDB(cfp);
            service = new AccountServiceImpl(ds);

        } catch ( final IOException e ) {
            e.printStackTrace();
        }
    }

    @AfterAll
    public static void removeSystemProperties() {
        System.clearProperty(ConfigurationProvider.SYSTEM_PROPERTY_KEY__CONF_FILE);
        System.clearProperty(ConfigurationProvider.SYSTEM_PROPERTY_KEY__KEY_FILE);
    }


    @Test
    @Order(0)
    public void t00_getAccount_shouldReturnNull_whenAccountDoesNotExist() throws AccountServiceException {
        assertNull(service.getAccount("nosuch@mail.com"));
    }

    @Test
    @Order(10)
    public void t01_createAccount_shouldCreateOneAccount_whenAccountDoesNotExist() throws AccountServiceException {
        final String email = "almafa@gmail.com";
        final String password = "password";
        final boolean locked = false;

        final int n = service.createAccount(email, password, locked);
        final AccountResultSetDto dto = service.getAccount(email);

        assertEquals(1, n);
        assertEquals(email, dto.email_address);
        assertEquals(locked, dto.account_locked);
    }

    @Test
    @Order(11)
    public void t01b_createAccount_shouldThrowAccountServiceException_whenAccountAlreadyExists() {
        assertThrows(AccountServiceException.class, () -> {
            final String email = "almafa@gmail.com";
            final String password = "password";
            final boolean locked = false;

            service.createAccount(email, password, locked);
        });
    }

    @Test
    @Order(20)
    public void t02_deletePreviousAccount_shouldDeleteAccount() throws AccountServiceException {
        final String email = "almafa@gmail.com";
        final int n = service.deleteAccount("almafa@gmail.com");

        assertNull(service.getAccount(email));
        assertEquals(1, n);
    }

    @Test
    @Order(30)
    public void t03_createAnotherAccount_shouldCreateAnotherAccount() throws AccountServiceException {
        final String email = "crud_tests@x-mail.com";
        final boolean locked = false;
        final String password = "password";

        final int n = service.createAccount(email, password, locked);
        final AccountResultSetDto dto = service.getAccount(email);


        assertEquals(1, n);
        assertEquals(dto.email_address, email);
        assertEquals(dto.account_locked, locked);
        assertEquals(dto.password_hash, Crypt.crypt(password, dto.password_hash));
    }

    @Test
    @Order(40)
    public void t04_setNewEmailAddressForPreviouslyCreatedAccount() throws AccountServiceException {
        final String email = "crud_tests@x-mail.com";
        final String newEmail = "my_new_crud_tests_mail@yahoo.com";
        final AccountResultSetDto beforeEmailChange;
        final AccountResultSetDto afterEmailChange;

        beforeEmailChange = service.getAccount(email);
        service.setAccountEmailAddress(email, newEmail);
        afterEmailChange = service.getAccount(newEmail);

        assertNull(service.getAccount(email));                              // old email should return null
        assertEquals(newEmail, afterEmailChange.email_address);            // get new email account should return account
        assertEquals(beforeEmailChange.account_id, afterEmailChange.account_id);    // account id should be same for the new email
    }

    @Test
    @Order(50)
    public void t05_lockAccount_shouldLockAccount() throws AccountServiceException {
        final String email_address = "my_new_crud_tests_mail@yahoo.com";
        final String reason = "naughty";

        final AccountResultSetDto beforeLock = service.getAccount(email_address);
        final int n = service.lockAccount(email_address, reason);
        final AccountResultSetDto afterLock = service.getAccount(email_address);


        assertEquals(1, n); // only one account should be locked

        assertEquals(email_address, beforeLock.email_address);
        assertFalse(beforeLock.account_locked);
        assertNull(beforeLock.account_lock_reason);

        assertEquals(email_address, afterLock.email_address);
        assertEquals(reason, afterLock.account_lock_reason);
        assertTrue(afterLock.account_locked);
    }

    @Test
    @Order(60)
    public void t06_unlockPreviouslyLockedAccount() throws AccountServiceException {
        final String email_address = "my_new_crud_tests_mail@yahoo.com";
        final String reason = "naughty";


        final AccountResultSetDto beforeUnlock = service.getAccount(email_address);
        final int n = service.unlockAccount(email_address);
        final AccountResultSetDto afterUnlock = service.getAccount(email_address);


        assertEquals(1, n);

        assertEquals(email_address, beforeUnlock.email_address);
        assertTrue(beforeUnlock.account_locked);
        assertEquals(reason, beforeUnlock.account_lock_reason);

        assertFalse(afterUnlock.account_locked);
        assertEquals("", afterUnlock.account_lock_reason);
    }

    @Test
    @Order(70)
    public void t07_changePassword() throws AccountServiceException {
        final String email = "my_new_crud_tests_mail@yahoo.com";
        final String password = "password";
        final String newPassword = "almafa1234#";

        final AccountResultSetDto beforePasswordChange = service.getAccount(email);
        final int n = service.setAccountPassword(email, newPassword);
        final AccountResultSetDto afterPasswordChange = service.getAccount(email);


        assertEquals(1, n);
        assertEquals(beforePasswordChange.password_hash, Crypt.crypt(password, beforePasswordChange.password_hash));
        assertEquals(afterPasswordChange.password_hash, Crypt.crypt(newPassword, afterPasswordChange.password_hash));
    }

    @Test
    @Order(80)
    public void t08_deleteAccount_shouldDeleteAccountWithNewEmail() throws AccountServiceException {
        final String email = "my_new_crud_tests_mail@yahoo.com";
        final int n = service.deleteAccount(email);
        final AccountResultSetDto afterDelete = service.getAccount(email);

        assertEquals(1, n);
        assertNull(afterDelete);
    }
}
