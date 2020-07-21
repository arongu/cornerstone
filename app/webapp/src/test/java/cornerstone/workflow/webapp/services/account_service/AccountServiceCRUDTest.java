package cornerstone.workflow.webapp.services.account_service;

import cornerstone.webapp.configuration.ConfigurationLoader;
import cornerstone.webapp.datasources.UsersDB;
import cornerstone.webapp.services.account.administration.AccountResultSet;
import cornerstone.webapp.services.account.administration.AccountAdministration;
import cornerstone.webapp.services.account.administration.AccountAdministrationException;
import cornerstone.webapp.services.account.administration.AccountAdministrationInterface;
import org.apache.commons.codec.digest.Crypt;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AccountServiceCRUDTest {
    private static AccountAdministrationInterface accountService;

    @BeforeAll
    public static void setSystemProperties() {
        final String test_files_dir = "../../_test_config/";
        final String confPath = Paths.get(test_files_dir + "app.conf").toAbsolutePath().normalize().toString();
        final String keyPath  = Paths.get(test_files_dir + "key.conf").toAbsolutePath().normalize().toString();

        System.setProperty(ConfigurationLoader.SYSTEM_PROPERTY_CONF_FILE, confPath);
        System.setProperty(ConfigurationLoader.SYSTEM_PROPERTY_KEY_FILE, keyPath);

        try {
            final ConfigurationLoader cr = new ConfigurationLoader();
            cr.loadAndDecryptConfig();

            final UsersDB ds = new UsersDB(cr);
            accountService = new AccountAdministration(ds);

        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    @AfterAll
    public static void removeSystemProperties() {
        System.clearProperty(ConfigurationLoader.SYSTEM_PROPERTY_CONF_FILE);
        System.clearProperty(ConfigurationLoader.SYSTEM_PROPERTY_KEY_FILE);
    }


    // -------------------------------------------- TCs --------------------------------------------
    @Test
    @Order(0)
    public void t00_get_shouldReturnNull_whenAccountDoesNotExist() throws AccountAdministrationException {
        assertNull(accountService.get("nosuch@mail.com"));
    }

    @Test
    @Order(10)
    public void t01_create_and_get_shouldCreateOneAccount_whenAccountDoesNotExist() throws AccountAdministrationException {
        final String email = "almafa@gmail.com";
        final String password = "password";
        final boolean locked = false;
        final boolean verified = true;
        final Timestamp ts = new Timestamp(System.currentTimeMillis());

        final int creates;
        final AccountResultSet account;


        creates = accountService.create(email, password, locked, verified);
        account = accountService.get(email);


        // Knowable value tests
        assertEquals(1, creates);
        assertEquals(email, account.email_address);
        assertEquals(locked, account.account_locked);
        assertNull(account.account_lock_reason);
        assertEquals(verified, account.email_address_verified);
        assertEquals(0, account.account_login_attempts);

        assertTrue(account.account_id > 0);
        assertEquals(account.email_address_ts, account.account_registration_ts); // happens same time
        assertEquals(account.email_address_ts, account.password_hash_ts);        // happens same time
        assertTrue(ts.before(account.account_registration_ts));
        assertTrue(ts.before(account.account_locked_ts));
        assertTrue(ts.before(account.password_hash_ts));
    }

    @Test
    @Order(11)
    public void t01b_create_shouldThrowAccountServiceException_whenAccountAlreadyExists() {
        assertThrows(AccountAdministrationException.class, () -> {
            final String email = "almafa@gmail.com";
            final String password = "password";
            final boolean locked = false;
            final boolean verified = true;

            accountService.create(email, password, locked, verified);
        });
    }

    @Test
    @Order(20)
    public void t02_delete_previousAccountShouldBeDeleted() throws AccountAdministrationException {
        final String email = "almafa@gmail.com";

        final int deletes;
        final AccountResultSet accountShouldBeDeleted;


        deletes = accountService.delete("almafa@gmail.com");
        accountShouldBeDeleted = accountService.get(email);


        assertEquals(1, deletes);
        assertNull(accountShouldBeDeleted);
    }

    @Test
    @Order(30)
    public void t03_create_anotherAccountShouldBeCreated() throws AccountAdministrationException {
        final String email = "crud_tests@x-mail.com";
        final String password = "password";
        final boolean locked = false;
        final boolean verified = true;

        final int creates;
        final AccountResultSet account;


        creates = accountService.create(email, password, locked, verified);
        account = accountService.get(email);


        assertEquals(1, creates);
        assertEquals(account.email_address, email);
        assertEquals(account.account_locked, locked);
        assertEquals(account.password_hash, Crypt.crypt(password, account.password_hash));
    }

    @Test
    @Order(40)
    public void t04_setNewEmailAddress_shouldSetNewEmailForPreviouslyCreatedAccount() throws AccountAdministrationException {
        final String email = "crud_tests@x-mail.com";
        final String newEmail = "my_new_crud_tests_mail@yahoo.com";

        final int emailChanges;
        final AccountResultSet beforeEmailChange;
        final AccountResultSet afterEmailChange;


        beforeEmailChange = accountService.get(email);
        emailChanges = accountService.setEmailAddress(email, newEmail);
        afterEmailChange = accountService.get(newEmail);


        assertEquals(1, emailChanges);
        assertNull(accountService.get(email));                                      // old email should return null
        assertEquals(newEmail, afterEmailChange.email_address);                     // get new email account should return account
        assertEquals(beforeEmailChange.account_id, afterEmailChange.account_id);    // account id should be same for the new email
    }

    @Test
    @Order(50)
    public void t05_lock_shouldLockAccount() throws AccountAdministrationException {
        final String email_address = "my_new_crud_tests_mail@yahoo.com";
        final String reason = "naughty";

        final int locks;
        final AccountResultSet beforeLock;
        final AccountResultSet afterLock;


        beforeLock = accountService.get(email_address);
        locks = accountService.lock(email_address, reason);
        afterLock = accountService.get(email_address);


        assertEquals(email_address, beforeLock.email_address);
        assertFalse(beforeLock.account_locked);
        assertNull(beforeLock.account_lock_reason);
        // only one account should be locked
        assertEquals(1, locks);
        // after lock
        assertEquals(email_address, afterLock.email_address);
        assertEquals(reason, afterLock.account_lock_reason);
        assertTrue(afterLock.account_locked);
    }

    @Test
    @Order(60)
    public void t06_unlock_shouldUnlockPreviouslyLockedAccount() throws AccountAdministrationException {
        final String email_address = "my_new_crud_tests_mail@yahoo.com";
        final String reason = "naughty";

        final int unlocks;
        final AccountResultSet beforeUnlock;
        final AccountResultSet afterUnlock;


        beforeUnlock = accountService.get(email_address);
        unlocks = accountService.unlock(email_address);
        afterUnlock = accountService.get(email_address);


        assertEquals(email_address, beforeUnlock.email_address);
        assertTrue(beforeUnlock.account_locked);
        assertEquals(reason, beforeUnlock.account_lock_reason);
        assertEquals(1, unlocks);
        assertFalse(afterUnlock.account_locked);
        assertNull(afterUnlock.account_lock_reason);
    }

    @Test
    @Order(70)
    public void t07_changePassword_shouldChangePasswordOfAccount() throws AccountAdministrationException {
        final String email = "my_new_crud_tests_mail@yahoo.com";
        final String password = "password";
        final String newPassword = "almafa1234#";

        final int passwordSets;
        final AccountResultSet beforePasswordChange;
        final AccountResultSet afterPasswordChange;


        beforePasswordChange = accountService.get(email);
        passwordSets = accountService.setPassword(email, newPassword);
        afterPasswordChange = accountService.get(email);


        assertEquals(1, passwordSets);
        assertEquals(beforePasswordChange.password_hash, Crypt.crypt(password, beforePasswordChange.password_hash));
        assertEquals(afterPasswordChange.password_hash, Crypt.crypt(newPassword, afterPasswordChange.password_hash));
    }

    @Test
    @Order(80)
    public void t08_delete_shouldDeleteAccount() throws AccountAdministrationException {
        final String email = "my_new_crud_tests_mail@yahoo.com";

        final int deletes;
        final AccountResultSet accountShouldBeDeleted;


        deletes = accountService.delete(email);
        accountShouldBeDeleted = accountService.get(email);


        assertEquals(1, deletes);
        assertNull(accountShouldBeDeleted);
    }
}
