package cornerstone.webapp.services.account.administration;

import cornerstone.webapp.configuration.ConfigurationLoader;
import cornerstone.webapp.datasources.UsersDB;
import org.apache.commons.codec.digest.Crypt;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AccountManager_CRUD_Test {
    private static AccountManagerInterface accountManager;

    @BeforeAll
    public static void setSystemProperties() {
        final String test_files_dir = "../../_test_config/";
        final String keyFile  = Paths.get(test_files_dir + "key.conf").toAbsolutePath().normalize().toString();
        final String confFile = Paths.get(test_files_dir + "app.conf").toAbsolutePath().normalize().toString();

        try {
            final ConfigurationLoader cr = new ConfigurationLoader(keyFile, confFile);
            cr.loadAndDecryptConfig();

            final UsersDB ds = new UsersDB(cr);
            accountManager = new AccountManager(ds, cr);

        } catch (final IOException e) {
            e.printStackTrace();
        }
    }


    // -------------------------------------------- TCs --------------------------------------------
    @Test
    @Order(0)
    public void t00_get_shouldThrwoNoSuchElementException_whenAccountDoesNotExist() throws AccountManagerException {
        assertThrows(NoSuchElementException.class, () -> accountManager.get("thereisnoway@suchemailexist.net"));
    }

    @Test
    @Order(10)
    public void t01_create_and_get_shouldCreateOneAccount_whenAccountDoesNotExist() throws AccountManagerException {
        final String email = "almafa@gmail.com";
        final String password = "password";
        final boolean locked = false;
        final boolean verified = true;
        final Timestamp ts = new Timestamp(System.currentTimeMillis());

        final int creates;
        final AccountResultSet account;


        creates = accountManager.create(email, password, locked, verified);
        account = accountManager.get(email);


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
        assertThrows(AccountManagerException.class, () -> {
            final String email = "almafa@gmail.com";
            final String password = "password";
            final boolean locked = false;
            final boolean verified = true;

            accountManager.create(email, password, locked, verified);
        });
    }

    @Test
    @Order(20)
    public void t02_delete_previousAccountShouldBeDeleted() throws AccountManagerException {
        final String email = "almafa@gmail.com";
        final int deletes;

        deletes = accountManager.delete("almafa@gmail.com");

        assertThrows(NoSuchElementException.class, () -> accountManager.get(email));
        assertEquals(1, deletes);
    }

    @Test
    @Order(30)
    public void t03_create_anotherAccountShouldBeCreated() throws AccountManagerException {
        final String email = "crud_tests@x-mail.com";
        final String password = "password";
        final boolean locked = false;
        final boolean verified = true;

        final int creates;
        final AccountResultSet account;


        creates = accountManager.create(email, password, locked, verified);
        account = accountManager.get(email);


        assertEquals(1, creates);
        assertEquals(account.email_address, email);
        assertEquals(account.account_locked, locked);
        assertEquals(account.password_hash, Crypt.crypt(password, account.password_hash));
    }

    @Test
    @Order(40)
    public void t04_setNewEmailAddress_shouldSetNewEmailForPreviouslyCreatedAccount() throws AccountManagerException {
        final String email = "crud_tests@x-mail.com";
        final String newEmail = "my_new_crud_tests_mail@yahoo.com";

        final int emailChanges;
        final AccountResultSet beforeEmailChange;
        final AccountResultSet afterEmailChange;


        beforeEmailChange = accountManager.get(email);
        emailChanges = accountManager.setEmailAddress(email, newEmail);
        afterEmailChange = accountManager.get(newEmail);


        assertEquals(1, emailChanges);
        assertThrows(NoSuchElementException.class, () -> accountManager.get(email)); // old email should throw exception
        assertEquals(newEmail, afterEmailChange.email_address);                      // get new email account should return account
        assertEquals(beforeEmailChange.account_id, afterEmailChange.account_id);     // account id should be same for the new email
    }

    @Test
    @Order(50)
    public void t05_lock_shouldLockAccount() throws AccountManagerException {
        final String email_address = "my_new_crud_tests_mail@yahoo.com";
        final String reason = "naughty";

        final int locks;
        final AccountResultSet beforeLock;
        final AccountResultSet afterLock;


        beforeLock = accountManager.get(email_address);
        locks = accountManager.lock(email_address, reason);
        afterLock = accountManager.get(email_address);


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
    public void t06_unlock_shouldUnlockPreviouslyLockedAccount() throws AccountManagerException {
        final String email_address = "my_new_crud_tests_mail@yahoo.com";
        final String reason = "naughty";

        final int unlocks;
        final AccountResultSet beforeUnlock;
        final AccountResultSet afterUnlock;


        beforeUnlock = accountManager.get(email_address);
        unlocks = accountManager.unlock(email_address);
        afterUnlock = accountManager.get(email_address);


        assertEquals(email_address, beforeUnlock.email_address);
        assertTrue(beforeUnlock.account_locked);
        assertEquals(reason, beforeUnlock.account_lock_reason);
        assertEquals(1, unlocks);
        assertFalse(afterUnlock.account_locked);
        assertNull(afterUnlock.account_lock_reason);
    }

    @Test
    @Order(70)
    public void t07_changePassword_shouldChangePasswordOfAccount() throws AccountManagerException {
        final String email = "my_new_crud_tests_mail@yahoo.com";
        final String password = "password";
        final String newPassword = "almafa1234#";

        final int passwordSets;
        final AccountResultSet beforePasswordChange;
        final AccountResultSet afterPasswordChange;


        beforePasswordChange = accountManager.get(email);
        passwordSets = accountManager.setPassword(email, newPassword);
        afterPasswordChange = accountManager.get(email);


        assertEquals(1, passwordSets);
        assertEquals(beforePasswordChange.password_hash, Crypt.crypt(password, beforePasswordChange.password_hash));
        assertEquals(afterPasswordChange.password_hash, Crypt.crypt(newPassword, afterPasswordChange.password_hash));
    }

    @Test
    @Order(80)
    public void t08_delete_shouldDeleteAccount() throws AccountManagerException {
        final int deletes;
        final String email = "my_new_crud_tests_mail@yahoo.com";


        deletes = accountManager.delete(email);


        assertEquals(1, deletes);
        assertThrows(NoSuchElementException.class, () -> accountManager.get(email));
    }
}