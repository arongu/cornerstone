package cornerstone.workflow.app.services.account_service;

import cornerstone.workflow.app.configuration.ConfigReader;
import cornerstone.workflow.app.datasource.DataSourceAccountDB;
import org.apache.commons.codec.digest.Crypt;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AccountServiceCRUDTest {
    private static AccountServiceInterface accountService;

    @BeforeAll
    public static void setSystemProperties() {
        final String dev_files_dir = "../../_dev_files/test_config/";
        final String confPath = Paths.get(dev_files_dir + "app.conf").toAbsolutePath().normalize().toString();
        final String keyPath = Paths.get(dev_files_dir + "key.conf").toAbsolutePath().normalize().toString();

        System.setProperty(ConfigReader.SYSTEM_PROPERTY_KEY__CONF_FILE, confPath);
        System.setProperty(ConfigReader.SYSTEM_PROPERTY_KEY__KEY_FILE, keyPath);

        try {
            final ConfigReader cp = new ConfigReader();
            cp.loadConfig();

            final DataSourceAccountDB ds = new DataSourceAccountDB(cp);
            accountService = new AccountService(ds);

        } catch ( final IOException e ) {
            e.printStackTrace();
        }
    }

    @AfterAll
    public static void removeSystemProperties() {
        System.clearProperty(ConfigReader.SYSTEM_PROPERTY_KEY__CONF_FILE);
        System.clearProperty(ConfigReader.SYSTEM_PROPERTY_KEY__KEY_FILE);
    }


    @Test
    @Order(0)
    public void t00_getAccount_shouldReturnNull_whenAccountDoesNotExist() throws AccountServiceException {
        assertNull(accountService.get("nosuch@mail.com"));
    }

    @Test
    @Order(10)
    public void t01_createAccount_shouldCreateOneAccount_whenAccountDoesNotExist() throws AccountServiceException {
        final String email = "almafa@gmail.com";
        final String password = "password";
        final boolean locked = false;
        final boolean verified = true;

        int n;
        final AccountResultSet account;


        n = accountService.create(email, password, locked, verified);
        account = accountService.get(email);


        assertEquals(1, n);
        assertEquals(email, account.email_address);
        assertEquals(locked, account.account_locked);
    }

    @Test
    @Order(11)
    public void t01b_createAccount_shouldThrowAccountServiceException_whenAccountAlreadyExists() {
        assertThrows(AccountServiceException.class, () -> {
            final String email = "almafa@gmail.com";
            final String password = "password";
            final boolean locked = false;
            final boolean verified = true;

            accountService.create(email, password, locked, verified);
        });
    }

    @Test
    @Order(20)
    public void t02_deletePreviousAccount_shouldDeleteAccount() throws AccountServiceException {
        final String email = "almafa@gmail.com";

        final int n;
        final AccountResultSet account;


        n = accountService.delete("almafa@gmail.com");
        account = accountService.get(email);


        assertNull(account);
        assertEquals(1, n);
    }

    @Test
    @Order(30)
    public void t03_createAnotherAccount_shouldCreateAnotherAccount() throws AccountServiceException {
        final String email = "crud_tests@x-mail.com";
        final String password = "password";
        final boolean locked = false;
        final boolean verified = true;

        final int n;
        final AccountResultSet account;


        n = accountService.create(email, password, locked, verified);
        account = accountService.get(email);


        assertEquals(1, n);
        assertEquals(account.email_address, email);
        assertEquals(account.account_locked, locked);
        assertEquals(account.password_hash, Crypt.crypt(password, account.password_hash));
    }

    @Test
    @Order(40)
    public void t04_setNewEmailAddressForPreviouslyCreatedAccount() throws AccountServiceException {
        final String email = "crud_tests@x-mail.com";
        final String newEmail = "my_new_crud_tests_mail@yahoo.com";

        final AccountResultSet beforeEmailChange;
        final AccountResultSet afterEmailChange;


        beforeEmailChange = accountService.get(email);
        accountService.setEmailAddress(email, newEmail);
        afterEmailChange = accountService.get(newEmail);


        assertNull(accountService.get(email));                                      // old email should return null
        assertEquals(newEmail, afterEmailChange.email_address);                     // get new email account should return account
        assertEquals(beforeEmailChange.account_id, afterEmailChange.account_id);    // account id should be same for the new email
    }

    @Test
    @Order(50)
    public void t05_lockAccount_shouldLockAccount() throws AccountServiceException {
        final String email_address = "my_new_crud_tests_mail@yahoo.com";
        final String reason = "naughty";

        final int n;
        final AccountResultSet beforeLock;
        final AccountResultSet afterLock;


        beforeLock = accountService.get(email_address);
        n = accountService.lock(email_address, reason);
        afterLock = accountService.get(email_address);



        assertEquals(email_address, beforeLock.email_address);
        assertFalse(beforeLock.account_locked);
        assertNull(beforeLock.account_lock_reason);
        // only one account should be locked
        assertEquals(1, n);
        // after lock
        assertEquals(email_address, afterLock.email_address);
        assertEquals(reason, afterLock.account_lock_reason);
        assertTrue(afterLock.account_locked);
    }

    @Test
    @Order(60)
    public void t06_unlockPreviouslyLockedAccount() throws AccountServiceException {
        final String email_address = "my_new_crud_tests_mail@yahoo.com";
        final String reason = "naughty";

        final AccountResultSet beforeUnlock;
        final AccountResultSet afterUnlock;
        final int n;


        beforeUnlock = accountService.get(email_address);
        n = accountService.unlock(email_address);
        afterUnlock = accountService.get(email_address);


        assertEquals(email_address, beforeUnlock.email_address);
        assertTrue(beforeUnlock.account_locked);
        assertEquals(reason, beforeUnlock.account_lock_reason);
        assertEquals(1, n);
        assertFalse(afterUnlock.account_locked);
        assertNull(afterUnlock.account_lock_reason);
    }

    @Test
    @Order(70)
    public void t07_changePassword() throws AccountServiceException {
        final String email = "my_new_crud_tests_mail@yahoo.com";
        final String password = "password";
        final String newPassword = "almafa1234#";

        final int n;
        final AccountResultSet beforePasswordChange;
        final AccountResultSet afterPasswordChange;


        beforePasswordChange = accountService.get(email);
        n = accountService.setPassword(email, newPassword);
        afterPasswordChange = accountService.get(email);


        assertEquals(1, n);
        assertEquals(beforePasswordChange.password_hash, Crypt.crypt(password, beforePasswordChange.password_hash));
        assertEquals(afterPasswordChange.password_hash, Crypt.crypt(newPassword, afterPasswordChange.password_hash));
    }

    @Test
    @Order(80)
    public void t08_deleteAccount_shouldDeleteAccountWithNewEmail() throws AccountServiceException {
        final String email = "my_new_crud_tests_mail@yahoo.com";
        final int n = accountService.delete(email);

        final AccountResultSet afterDelete;


        afterDelete = accountService.get(email);


        assertEquals(1, n);
        assertNull(afterDelete);
    }
}
