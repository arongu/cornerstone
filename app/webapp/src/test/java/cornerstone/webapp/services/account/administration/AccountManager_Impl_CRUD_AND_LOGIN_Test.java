package cornerstone.webapp.services.account.administration;

import cornerstone.webapp.configuration.ConfigurationLoader;
import cornerstone.webapp.configuration.enums.APP_ENUM;
import cornerstone.webapp.datasources.UsersDB;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

// THIS IS MORE THAN JUST A SINGLE LOGIN TEST, IT HEAVILY RELIES ON CRUD METHODS
// ALL TCS CLEANUP AFTER THEMSELVES FOR DATABASE AND TEST CONSISTENCY!
public class AccountManager_Impl_CRUD_AND_LOGIN_Test {
    private static AccountManager accountManager;
    private static int MAX_LOGIN_ATTEMPTS_FROM_TEST_CONFIG;

    @BeforeAll
    public static void setSystemProperties() {
        final String test_files_dir = "../../_test_config/";
        final String keyFile  = Paths.get(test_files_dir + "key.conf").toAbsolutePath().normalize().toString();
        final String confFile = Paths.get(test_files_dir + "app.conf").toAbsolutePath().normalize().toString();

        try {
            final ConfigurationLoader cr = new ConfigurationLoader(keyFile, confFile);
            cr.loadAndDecryptConfig();

            final UsersDB ds = new UsersDB(cr);
            accountManager = new AccountManagerImpl(ds, cr);
            MAX_LOGIN_ATTEMPTS_FROM_TEST_CONFIG = Integer.parseInt(cr.getAppProperties().getProperty(APP_ENUM.APP_MAX_LOGIN_ATTEMPTS.key));

        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void login_shouldReturnTrue_whenAccountExistsNotLockedAndVerified() throws AccountManagerSqlException {
        final String email = "melchior@login.me";
        final String password = "miciMacko#";
        final boolean locked = false;
        final boolean verified = true;

        final boolean login_result;
        final int number_of_accounts_created;
        final int number_of_accounts_deleted;



        number_of_accounts_created = accountManager.create(email, password, locked, verified);
        login_result               = accountManager.login(email, password);
        number_of_accounts_deleted = accountManager.delete(email);
        assertThrows(NoSuchElementException.class, () -> accountManager.get(email));



        assertEquals(1, number_of_accounts_created);
        assertTrue(login_result);
        assertEquals(1, number_of_accounts_deleted);
    }

    @Test
    public void login_shouldReturnFalse_whenAccountDoesNotExist() throws AccountManagerSqlException {
        final String email = "xxxxx@doesnotexist.xu";
        final String password = "wow";
        final boolean login_result;


        login_result = accountManager.login(email, password);
        assertThrows(NoSuchElementException.class, () -> accountManager.get(email));


        assertFalse(login_result);
    }

    @Test
    public void login_shouldReturnFalse_whenAccountNotVerifiedAndNotLocked() throws AccountManagerSqlException {
        final String email = "casper@login.me";
        final String password = "casper#";
        final boolean locked = false;
        final boolean verified = false;

        final boolean login_result;
        final int number_of_accounts_created;
        final int number_of_accounts_deleted;
        final AccountResultSet created_account;



        number_of_accounts_created = accountManager.create(email, password, locked, verified);
        created_account            = accountManager.get(email);
        login_result               = accountManager.login(email, password);
        number_of_accounts_deleted = accountManager.delete(email);



        assertEquals(1, number_of_accounts_created);
        assertFalse(login_result);
        assertEquals(email, created_account.email_address);
        assertFalse(created_account.account_locked);
        assertFalse(created_account.email_address_verified);
        assertEquals(1, number_of_accounts_deleted);
        assertThrows(NoSuchElementException.class, () -> accountManager.get(email)); // verify last delete
    }

    @Test
    public void login_shouldReturnFalse_whenAccountLocked() throws AccountManagerSqlException {
        final String email = "locked@login.me";
        final String password = "locked#";
        final boolean locked = true;
        final boolean verified = true;

        final boolean login_result;
        final int number_of_accounts_created;
        final int number_of_accounts_deleted;
        final AccountResultSet created_account;



        number_of_accounts_created = accountManager.create(email, password, locked, verified);
        created_account            = accountManager.get(email);
        login_result               = accountManager.login(email, password);
        number_of_accounts_deleted = accountManager.delete(email);



        assertEquals(1, number_of_accounts_created);
        assertTrue(created_account.account_locked);
        assertFalse(login_result);
        assertEquals(1, number_of_accounts_deleted);
        assertThrows(NoSuchElementException.class, () -> accountManager.get(email)); // verify last account deletion
    }

    @Test
    public void login_shouldIncrementLoginAttempts_whenLoginFails() throws AccountManagerSqlException {
        final String email = "badtyper@login.me";
        final String password = "secretpasswordd#";
        final String bad_password = "myBadPassword#";
        final boolean locked = false;
        final boolean verified = true;

        final boolean[] logins = new boolean[3];
        final int number_of_accounts_created;
        final int number_of_accounts_deleted;
        final AccountResultSet account_after_bad_logins;



        number_of_accounts_created = accountManager.create(email, password, locked, verified);
        logins[0]                  = accountManager.login(email, bad_password);
        logins[1]                  = accountManager.login(email, bad_password);
        logins[2]                  = accountManager.login(email, bad_password);
        account_after_bad_logins   = accountManager.get(email);
        number_of_accounts_deleted = accountManager.delete(email);



        assertEquals(1, number_of_accounts_created);
        assertEquals(3, account_after_bad_logins.account_login_attempts);
        assertFalse(logins[0]);
        assertFalse(logins[1]);
        assertFalse(logins[2]);
        assertEquals(1, number_of_accounts_deleted);
        assertThrows(NoSuchElementException.class, () -> accountManager.get(email)); // verify last delete
    }

    @Test
    public void clearLoginAttempts_shouldClearLoginAttempts_whenCalled() throws AccountManagerSqlException {
        final String email = "badtyper@login.me";
        final String password = "secretpasswordd#";
        final String bad_password = "myBadPassword#";
        final boolean locked = false;
        final boolean verified = true;

        final int number_of_accounts_created;
        final int number_of_accounts_cleared;
        final int number_of_accounts_deleted;
        boolean first__login_good_password;
        boolean second_login_bad_password;
        boolean third__login_bad_password;
        final AccountResultSet account_before_clear;
        final AccountResultSet account_after_clear;



        number_of_accounts_created = accountManager.create(email, password, locked, verified);
        first__login_good_password = accountManager.login(email, password);
        second_login_bad_password  = accountManager.login(email, bad_password);
        third__login_bad_password  = accountManager.login(email, bad_password);
        account_before_clear       = accountManager.get(email);
        number_of_accounts_cleared = accountManager.clearLoginAttempts(email);
        account_after_clear        = accountManager.get(email);
        number_of_accounts_deleted = accountManager.delete(email);



        assertEquals(1, number_of_accounts_created);
        assertTrue(first__login_good_password);
        assertFalse(second_login_bad_password);
        assertFalse(third__login_bad_password);
        assertEquals(2, account_before_clear.account_login_attempts);
        assertEquals(1, number_of_accounts_cleared);
        assertEquals(0, account_after_clear.account_login_attempts);
        assertEquals(1, number_of_accounts_deleted);
        assertThrows(NoSuchElementException.class, () -> accountManager.get(email)); // verify last delete
    }

    @Test
    public void login_shouldIncrementLoginAttemptsToLessThanMaxLoginAndAccountShouldNotBeLocked_whenFailedToLoginThatManyTimes() throws AccountManagerSqlException {
        final String email = "badtyper180@login.me";
        final String password = "secretpasswordd#";
        final String bad_password = "alma";
        final boolean locked = false;
        final boolean verified = true;

        final int number_of_accounts_created;
        final int number_of_accounts_deleted;
        final boolean[] logins = new boolean[MAX_LOGIN_ATTEMPTS_FROM_TEST_CONFIG];
        final AccountResultSet created_account;



        number_of_accounts_created = accountManager.create(email, password, locked, verified);
        for (int i = 0; i < MAX_LOGIN_ATTEMPTS_FROM_TEST_CONFIG; i++ ) {
            logins[i] = accountManager.login(email, bad_password);
        }
        created_account = accountManager.get(email);
        number_of_accounts_deleted = accountManager.delete(email);



        assertEquals(1, number_of_accounts_created);
        assertEquals(MAX_LOGIN_ATTEMPTS_FROM_TEST_CONFIG, created_account.account_login_attempts);
        assertFalse(created_account.account_locked);
        assertEquals(1, number_of_accounts_deleted);
        assertThrows(NoSuchElementException.class, () -> accountManager.get(email)); // verify deletion
        for (final boolean login : logins) {
            assertFalse(login);
        }
    }

    @Test
    public void login_shouldLockAccount_whenMaxFailedLoginAttemptsExceeded() throws AccountManagerSqlException {
        final String email = "autolock180@login.me";
        final String password = "secretpasswordd#";
        final String bad_password = "alma";
        final boolean locked = false;
        final boolean verified = true;

        final int number_of_accounts_created;
        final int number_of_accounts_deleted;
        final boolean first_login_good_password;
        final boolean login_with_locked_account_good_password;
        final AccountResultSet account_without_bad_logins;
        final AccountResultSet account_after_lock;



        number_of_accounts_created  = accountManager.create(email, password, locked, verified);
        first_login_good_password   = accountManager.login(email, password);
        account_without_bad_logins  = accountManager.get(email);
        for (int i = 0; i < MAX_LOGIN_ATTEMPTS_FROM_TEST_CONFIG + 20; i++ ) {
            accountManager.login(email, bad_password);
        }
        login_with_locked_account_good_password = accountManager.login(email, password);
        account_after_lock                      = accountManager.get(email);
        number_of_accounts_deleted              = accountManager.delete(email);
        assertThrows(NoSuchElementException.class, () -> accountManager.get(email));



        assertEquals(1, number_of_accounts_created);
        assertTrue(first_login_good_password);
        assertFalse(account_without_bad_logins.account_locked);
        assertEquals(0, account_without_bad_logins.account_login_attempts);
        assertFalse(login_with_locked_account_good_password);
        assertTrue(account_after_lock.account_locked);
        assertEquals(MAX_LOGIN_ATTEMPTS_FROM_TEST_CONFIG, account_after_lock.account_login_attempts);
        assertEquals(1, number_of_accounts_deleted);
    }

    @Test
    public void login_loginAttemptsShouldResetToZero_whenLoginIsSuccess() throws AccountManagerSqlException {
        final String email = "lastnite@aaa.me";
        final String password = "woho#";
        final String bad_password = "bbbbb";
        final boolean locked = false;
        final boolean verified = true;

        final int number_of_accounts_created;
        final int number_of_accounts_deleted;
        final boolean first__login_bad_password;
        final boolean second_login_bad_password;
        final boolean third__login_bad_password;
        final boolean fourth_login_good_password;
        final AccountResultSet account_after_three_bad_logins;
        final AccountResultSet account_after_good_login_should_be_reset;



        number_of_accounts_created                = accountManager.create(email, password, locked, verified);
        first__login_bad_password                 = accountManager.login(email, bad_password);
        second_login_bad_password                = accountManager.login(email, bad_password);
        third__login_bad_password                 = accountManager.login(email, bad_password);
        account_after_three_bad_logins           = accountManager.get(email);
        fourth_login_good_password               = accountManager.login(email, password);
        account_after_good_login_should_be_reset = accountManager.get(email);
        number_of_accounts_deleted                = accountManager.delete(email);



        assertEquals(1, number_of_accounts_created);
        assertFalse(first__login_bad_password);
        assertFalse(second_login_bad_password);
        assertFalse(third__login_bad_password);
        assertEquals(3, account_after_three_bad_logins.account_login_attempts);
        assertTrue(fourth_login_good_password);
        assertEquals(0, account_after_good_login_should_be_reset.account_login_attempts);
        assertEquals(1, number_of_accounts_deleted);
        assertThrows(NoSuchElementException.class, () -> accountManager.get(email)); // verify last delete
    }
}
