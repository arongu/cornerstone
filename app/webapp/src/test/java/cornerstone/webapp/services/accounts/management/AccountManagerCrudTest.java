package cornerstone.webapp.services.accounts.management;

import cornerstone.webapp.configuration.ConfigLoader;
import cornerstone.webapp.datasources.UsersDB;
import cornerstone.webapp.services.accounts.management.exceptions.single.CreationDuplicateException;
import cornerstone.webapp.services.accounts.management.exceptions.single.CreationNullException;
import cornerstone.webapp.services.accounts.management.exceptions.single.NoAccountException;
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
        final String keyFile        = Paths.get(test_files_dir + "key.conf").toAbsolutePath().normalize().toString();
        final String confFile       = Paths.get(test_files_dir + "app.conf").toAbsolutePath().normalize().toString();

        try {
            final ConfigLoader cr = new ConfigLoader(keyFile, confFile);
            final UsersDB ds      = new UsersDB(cr);
            accountManager        = new AccountManagerImpl(ds, cr);

        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    // -------------------------------------------- TCs --------------------------------------------
    @Test
    @Order(0)
    public void t00_get_shouldThrowAccountDoesNotExistException_whenAccountDoesNotExist() {
        final NoAccountException e = assertThrows(NoAccountException.class, () -> accountManager.get("thereisnoway@suchemailexist.net"));
        assertEquals("Account 'thereisnoway@suchemailexist.net' does not exist.", e.getMessage());
    }

    @Test
    @Order(10)
    public void t01a_create_and_get_shouldCreateOneAccount_whenAccountDoesNotExist() throws Exception {
        final String email     = "almafa@gmail.com";
        final String password  = "password";
        final boolean locked   = false;
        final boolean verified = true;
        final Timestamp ts     = new Timestamp(System.currentTimeMillis());
        final UserRole role = UserRole.USER;
        // results
        final int number_of_accounts_created;
        final AccountResultSet received_account;
        // delete if exists
        TestHelper.deleteAccount(accountManager, email);


        number_of_accounts_created = accountManager.create(email, password, locked, verified, role);
        received_account           = accountManager.get(email);


        // Knowable value tests
        assertEquals(1, number_of_accounts_created);
        assertEquals(email, received_account.email_address);
        assertEquals(locked, received_account.account_locked);
        assertNull(received_account.account_lock_reason);
        assertEquals(verified, received_account.email_address_verified);
        assertEquals(0, received_account.account_login_attempts);
        assertEquals(role.getId(), received_account.role_id);
        // Dynamic value tests
        assertTrue(received_account.account_id > 0);
        assertEquals(received_account.email_address_ts, received_account.account_registration_ts); // happens same time
        assertEquals(received_account.email_address_ts, received_account.password_hash_ts);        // happens same time
        assertTrue(ts.before(received_account.account_registration_ts));
        assertTrue(ts.before(received_account.account_locked_ts));
        assertTrue(ts.before(received_account.password_hash_ts));
    }

    @Test
    @Order(11)
    public void t01b_create_shouldThrowAccountCreationDuplicateException_whenAccountAlreadyExists() {
        final CreationDuplicateException e = assertThrows(CreationDuplicateException.class, () -> {
            final String email            = "almafa@gmail.com";
            final String password         = "password";
            final boolean locked          = false;
            final boolean verified        = true;
            final UserRole accountRole = UserRole.USER;

            accountManager.create(email, password, locked, verified, accountRole);
        });
        assertEquals("Failed to create 'almafa@gmail.com' (already exists).", e.getMessage());
    }

    @Test
    @Order(20)
    public void t02_delete_previousAccountShouldBeDeleted() throws Exception {
        final String email = "almafa@gmail.com";
        final int number_of_accounts_deleted;


        number_of_accounts_deleted = accountManager.delete("almafa@gmail.com");


        final NoAccountException e = assertThrows(NoAccountException.class, () -> accountManager.get(email));
        assertEquals(1, number_of_accounts_deleted);
        assertEquals("Account 'almafa@gmail.com' does not exist." , e.getMessage());
    }

    @Test
    @Order(30)
    public void t03_create_anotherAccountShouldBeCreated() throws Exception {
        final String email            = "crud_tests@x-mail.com";
        final String password         = "password";
        final boolean locked          = false;
        final boolean verified        = true;
        final UserRole accountRole = UserRole.SUPER;
        // results
        final int number_of_accounts_created;
        final AccountResultSet received_account;


        number_of_accounts_created = accountManager.create(email, password, locked, verified, accountRole);
        received_account           = accountManager.get(email);


        assertEquals(1, number_of_accounts_created);
        assertEquals(email, received_account.email_address);
        assertEquals(locked, received_account.account_locked);
        assertEquals(accountRole.getId(), received_account.role_id);
        assertEquals(verified, received_account.email_address_verified);
        assertEquals(received_account.password_hash, Crypt.crypt(password, received_account.password_hash));
    }

    @Test
    @Order(40)
    public void t04_setNewEmailAddress_shouldSetNewEmailForPreviouslyCreatedAccount() throws Exception {
        final String email     = "crud_tests@x-mail.com";
        final String new_email = "my_new_crud_tests_mail@yahoo.com";
        // results
        final int number_of_email_changes;
        final AccountResultSet beforeEmailChange;
        final AccountResultSet afterEmailChange;


        beforeEmailChange       = accountManager.get(email);
        number_of_email_changes = accountManager.setEmail(email, new_email);
        afterEmailChange        = accountManager.get(new_email);


        assertEquals(1, number_of_email_changes);
        assertThrows(NoAccountException.class, () -> accountManager.get(email)); // old email should throw exception
        assertEquals(new_email, afterEmailChange.email_address);                           // get new email account should return account
        assertEquals(beforeEmailChange.account_id, afterEmailChange.account_id);           // account id should be same for the new email
    }

    @Test
    @Order(50)
    public void t05_lock_shouldLockAccount() throws Exception {
        final String email_address = "my_new_crud_tests_mail@yahoo.com";
        final String reason        = "naughty";
        // results
        final int locks;
        final AccountResultSet beforeLock;
        final AccountResultSet afterLock;


        beforeLock = accountManager.get(email_address);
        locks      = accountManager.lock(email_address, reason);
        afterLock  = accountManager.get(email_address);


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
    public void t06_unlock_shouldUnlockPreviouslyLockedAccount() throws Exception {
        final String email_address = "my_new_crud_tests_mail@yahoo.com";
        final String reason        = "naughty";
        // results
        final int unlocks;
        final AccountResultSet beforeUnlock;
        final AccountResultSet afterUnlock;


        beforeUnlock = accountManager.get(email_address);
        unlocks      = accountManager.unlock(email_address);
        afterUnlock  = accountManager.get(email_address);


        assertEquals(email_address, beforeUnlock.email_address);
        assertTrue(beforeUnlock.account_locked);
        assertEquals(reason, beforeUnlock.account_lock_reason);
        assertEquals(1, unlocks);
        assertFalse(afterUnlock.account_locked);
        assertNull(afterUnlock.account_lock_reason);
    }

    @Test
    @Order(70)
    public void t07_changePassword_shouldChangePasswordOfAccount() throws Exception {
        final String email       = "my_new_crud_tests_mail@yahoo.com";
        final String password    = "password";
        final String newPassword = "almafa1234#";
        // results
        final int number_of_password_sets;
        final AccountResultSet beforePasswordChange;
        final AccountResultSet afterPasswordChange;


        beforePasswordChange    = accountManager.get(email);
        number_of_password_sets = accountManager.setPassword(email, newPassword);
        afterPasswordChange     = accountManager.get(email);


        assertEquals(1, number_of_password_sets);
        assertEquals(beforePasswordChange.password_hash, Crypt.crypt(password, beforePasswordChange.password_hash));
        assertEquals(afterPasswordChange.password_hash, Crypt.crypt(newPassword, afterPasswordChange.password_hash));
    }

    @Test
    @Order(80)
    public void t08_setRole_shouldUpdateRole() throws Exception {
        final String email = "my_new_crud_tests_mail@yahoo.com";
        // results
        int number_of_updates = 0;
        final AccountResultSet beforeUpdate;
        final AccountResultSet afterUpdate1;
        final AccountResultSet afterUpdate2;
        final AccountResultSet afterUpdate3;
        final AccountResultSet afterUpdate4;


        beforeUpdate       = accountManager.get(email);
        number_of_updates += accountManager.setRole(email, UserRole.NO_ROLE);
        afterUpdate1       = accountManager.get(email);
        number_of_updates += accountManager.setRole(email, UserRole.USER);
        afterUpdate2       = accountManager.get(email);
        number_of_updates += accountManager.setRole(email, UserRole.SUPER);
        afterUpdate3       = accountManager.get(email);
        number_of_updates += accountManager.setRole(email, UserRole.ADMIN);
        afterUpdate4       = accountManager.get(email);


        assertEquals(4, number_of_updates);
        assertEquals(UserRole.SUPER.getId(), beforeUpdate.role_id);
        assertEquals(UserRole.NO_ROLE.getId(), afterUpdate1.role_id);
        assertEquals(UserRole.USER.getId(), afterUpdate2.role_id);
        assertEquals(UserRole.SUPER.getId(), afterUpdate3.role_id);
        assertEquals(UserRole.ADMIN.getId(), afterUpdate4.role_id);
    }

    @Test
    @Order(90)
    public void t09_delete_shouldDeleteAccount() throws Exception {
        final String email = "my_new_crud_tests_mail@yahoo.com";
        final int number_of_deletes;


        number_of_deletes = accountManager.delete(email);


        assertEquals(1, number_of_deletes);
        final NoAccountException e = assertThrows(NoAccountException.class, () -> accountManager.get(email));
        assertEquals("Account 'my_new_crud_tests_mail@yahoo.com' does not exist.", e.getMessage());
    }

    @Test
    @Order(100)
    public void t10_create_shouldThrowNoAccountException_whenCreateHasNullAsValue() throws Exception {
        final String email = "blabla@xxx.com";


        assertThrows(CreationNullException.class, () -> accountManager.create(email, null, false, true, UserRole.USER));
        assertThrows(CreationNullException.class, () -> accountManager.create(null, "password", false, true, UserRole.USER));
        assertThrows(CreationNullException.class, () -> accountManager.create(email, "password", false, true, null));
        assertThrows(CreationNullException.class, () -> accountManager.create(null, null, false, true, UserRole.SUPER));
        assertThrows(CreationNullException.class, () -> accountManager.create(null, null, false, true, null));


        final NoAccountException e = assertThrows(NoAccountException.class, () -> accountManager.get(email));
        assertEquals("Account 'blabla@xxx.com' does not exist.", e.getMessage());
    }
}
