package cornerstone.webapp.service.account.administration;

import cornerstone.webapp.config.ConfigLoader;
import cornerstone.webapp.datasources.UsersDB;
import cornerstone.webapp.rest.endpoint.account.AccountDeletionException;
import cornerstone.webapp.service.account.administration.exceptions.single.*;
import org.apache.commons.codec.digest.Crypt;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AccountManagerCrudTest {
    private static AccountManager accountManager;

    @BeforeAll
    public static void setSystemProperties() {
        final String test_files_dir = "../../_test_config/";
        final String keyFile  = Paths.get(test_files_dir + "key.conf").toAbsolutePath().normalize().toString();
        final String confFile = Paths.get(test_files_dir + "app.conf").toAbsolutePath().normalize().toString();

        try {
            final ConfigLoader cr = new ConfigLoader(keyFile, confFile);
            final UsersDB ds = new UsersDB(cr);
            accountManager = new AccountManagerImpl(ds, cr);

        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    // -------------------------------------------- TCs --------------------------------------------
    @Test
    @Order(0)
    public void t00_get_shouldThrowAccountDoesNotExistException_whenAccountDoesNotExist() {
        assertThrows(AccountDoesNotExistException.class, () -> accountManager.get("thereisnoway@suchemailexist.net"));
    }

    @Test
    @Order(10)
    public void t01a_create_and_get_shouldCreateOneAccount_whenAccountDoesNotExist() throws
            AccountDoesNotExistException, AccountDeletionException,
            AccountRetrievalException, AccountCreationException {

        final String email = "almafa@gmail.com";
        final String password = "password";
        final boolean locked = false;
        final boolean verified = true;
        final Timestamp ts = new Timestamp(System.currentTimeMillis());

        final int number_of_account_created;
        final AccountResultSet received_account;


        accountManager.delete(email);
        number_of_account_created = accountManager.create(email, password, locked, verified);
        received_account = accountManager.get(email);


        // Knowable value tests
        assertEquals(1, number_of_account_created);
        assertEquals(email, received_account.email_address);
        assertEquals(locked, received_account.account_locked);
        assertNull(received_account.account_lock_reason);
        assertEquals(verified, received_account.email_address_verified);
        assertEquals(0, received_account.account_login_attempts);

        assertTrue(received_account.account_id > 0);
        assertEquals(received_account.email_address_ts, received_account.account_registration_ts); // happens same time
        assertEquals(received_account.email_address_ts, received_account.password_hash_ts);        // happens same time
        assertTrue(ts.before(received_account.account_registration_ts));
        assertTrue(ts.before(received_account.account_locked_ts));
        assertTrue(ts.before(received_account.password_hash_ts));
    }

    @Test
    @Order(11)
    public void t01b_create_shouldThrowAccountCreationException_whenAccountAlreadyExists() {
        assertThrows(AccountCreationException.class, () -> {
            final String email = "almafa@gmail.com";
            final String password = "password";
            final boolean locked = false;
            final boolean verified = true;

            accountManager.create(email, password, locked, verified);
        });
    }

    @Test
    @Order(20)
    public void t02_delete_previousAccountShouldBeDeleted() throws AccountDeletionException {
        final String email = "almafa@gmail.com";
        final int number_of_account_deleted;

        number_of_account_deleted = accountManager.delete("almafa@gmail.com");

        assertThrows(AccountDoesNotExistException.class, () -> accountManager.get(email));
        assertEquals(1, number_of_account_deleted);
    }

    @Test
    @Order(30)
    public void t03_create_anotherAccountShouldBeCreated() throws AccountDoesNotExistException, AccountCreationException, AccountRetrievalException {
        final String email = "crud_tests@x-mail.com";
        final String password = "password";
        final boolean locked = false;
        final boolean verified = true;

        final int number_of_account_created;
        final AccountResultSet received_account;


        number_of_account_created = accountManager.create(email, password, locked, verified);
        received_account = accountManager.get(email);


        assertEquals(1, number_of_account_created);
        assertEquals(received_account.email_address, email);
        assertEquals(received_account.account_locked, locked);
        assertEquals(received_account.password_hash, Crypt.crypt(password, received_account.password_hash));
    }

    @Test
    @Order(40)
    public void t04_setNewEmailAddress_shouldSetNewEmailForPreviouslyCreatedAccount() throws AccountDoesNotExistException, AccountRetrievalException, UpdateEmailException {
        final String email = "crud_tests@x-mail.com";
        final String new_email = "my_new_crud_tests_mail@yahoo.com";

        final int number_of_email_changes;
        final AccountResultSet beforeEmailChange;
        final AccountResultSet afterEmailChange;


        beforeEmailChange = accountManager.get(email);
        number_of_email_changes = accountManager.setEmail(email, new_email);
        afterEmailChange = accountManager.get(new_email);


        assertEquals(1, number_of_email_changes);
        assertThrows(AccountDoesNotExistException.class, () -> accountManager.get(email)); // old email should throw exception
        assertEquals(new_email, afterEmailChange.email_address);                           // get new email account should return account
        assertEquals(beforeEmailChange.account_id, afterEmailChange.account_id);           // account id should be same for the new email
    }

    @Test
    @Order(50)
    public void t05_lock_shouldLockAccount() throws AccountDoesNotExistException, AccountRetrievalException, UpdateLockException {
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
    public void t06_unlock_shouldUnlockPreviouslyLockedAccount() throws AccountDoesNotExistException, AccountRetrievalException, UpdateLockException {
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
    public void t07_changePassword_shouldChangePasswordOfAccount() throws AccountDoesNotExistException, AccountRetrievalException, UpdatePasswordException {
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
    public void t08_delete_shouldDeleteAccount() throws AccountDeletionException {
        final int deletes;
        final String email = "my_new_crud_tests_mail@yahoo.com";


        deletes = accountManager.delete(email);


        assertEquals(1, deletes);
        assertThrows(AccountDoesNotExistException.class, () -> accountManager.get(email));
    }
}
