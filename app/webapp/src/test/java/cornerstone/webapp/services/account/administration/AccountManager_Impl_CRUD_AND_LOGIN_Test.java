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
    public void login_shouldReturnTrue_whenAccountExistsNotLockedAndVerified() throws AccountManagerException {
        final String email = "melchior@login.me";
        final String password = "miciMacko#";
        final boolean locked = false;
        final boolean verified = true;

        final boolean loggedIn;
        final int creates;
        final int deletes;


        creates = accountManager.create(email, password, locked, verified);
        loggedIn = accountManager.login(email, password);
        deletes = accountManager.delete(email);
        assertThrows(NoSuchElementException.class, () -> accountManager.get(email));


        assertEquals(1, creates);
        assertTrue(loggedIn);
        assertEquals(1, deletes);
    }

    @Test
    public void login_shouldReturnFalse_whenAccountDoesNotExist() throws AccountManagerException {
        final String email = "xxxxx@doesnotexist.xu";
        final String password = "wow";

        final boolean loggedIn;


        loggedIn = accountManager.login(email, password);
        assertThrows(NoSuchElementException.class, () -> accountManager.get(email));


        assertFalse(loggedIn);
    }

    @Test
    public void login_shouldReturnFalse_whenAccountNotVerifiedAndNotLocked() throws AccountManagerException {
        final String email = "casper@login.me";
        final String password = "casper#";
        final boolean locked = false;
        final boolean verified = false;

        final boolean loggedIn;
        final int creates;
        final int deletes;
        final AccountResultSet account;


        creates = accountManager.create(email, password, locked, verified);
        account = accountManager.get(email);
        loggedIn = accountManager.login(email, password);
        deletes = accountManager.delete(email);
        assertThrows(NoSuchElementException.class, () -> accountManager.get(email));


        assertEquals(1, creates);
        assertFalse(loggedIn);
        assertEquals(email, account.email_address);
        assertFalse(account.account_locked);
        assertFalse(account.email_address_verified);
        assertEquals(1, deletes);
    }

    @Test
    public void login_shouldReturnFalse_whenAccountLocked() throws AccountManagerException {
        final String email = "locked@login.me";
        final String password = "locked#";
        final boolean locked = true;
        final boolean verified = true;

        final boolean loggedIn;
        final int creates;
        final int deletes;
        final AccountResultSet account;


        creates = accountManager.create(email, password, locked, verified);
        account = accountManager.get(email);
        loggedIn = accountManager.login(email, password);
        deletes = accountManager.delete(email);
        assertThrows(NoSuchElementException.class, () -> accountManager.get(email));


        assertEquals(1, creates);
        assertTrue(account.account_locked);
        assertFalse(loggedIn);
        assertEquals(1, deletes);
    }

    @Test
    public void login_shouldIncrementLoginAttempts_whenLoginFails() throws AccountManagerException {
        final String email = "badtyper@login.me";
        final String password = "secretpasswordd#";
        final String badPassword = "myBadPassword#";
        final boolean locked = false;
        final boolean verified = true;

        final boolean[] logins = new boolean[3];
        final int creates;
        final int deletes;
        final AccountResultSet accountAfterBadLogins;


        creates = accountManager.create(email, password, locked, verified);
        logins[0] = accountManager.login(email, badPassword);
        logins[1] = accountManager.login(email, badPassword);
        logins[2] = accountManager.login(email, badPassword);
        accountAfterBadLogins = accountManager.get(email);
        deletes = accountManager.delete(email);
        assertThrows(NoSuchElementException.class, () -> accountManager.get(email));


        assertEquals(1, creates);
        assertEquals(3, accountAfterBadLogins.account_login_attempts);
        assertFalse(logins[0]);
        assertFalse(logins[1]);
        assertFalse(logins[2]);
        assertEquals(1, deletes);
    }

    @Test
    public void clearLoginAttempts_shouldClearLoginAttempts_whenCalled() throws AccountManagerException {
        final String email = "badtyper@login.me";
        final String password = "secretpasswordd#";
        final String badPassword = "myBadPassword#";
        final boolean locked = false;
        final boolean verified = true;

        final int creates;
        final int clears;
        final int deletes;
        boolean firstGoodLogin;
        boolean secondBadLogin;
        boolean thirdBadLogin;
        final AccountResultSet accountBeforeClear;
        final AccountResultSet accountAfterClear;


        creates = accountManager.create(email, password, locked, verified);
        firstGoodLogin = accountManager.login(email, password);
        secondBadLogin = accountManager.login(email, badPassword);
        thirdBadLogin = accountManager.login(email, badPassword);
        accountBeforeClear = accountManager.get(email);
        clears = accountManager.clearLoginAttempts(email);
        accountAfterClear = accountManager.get(email);
        deletes = accountManager.delete(email);
        assertThrows(NoSuchElementException.class, () -> accountManager.get(email));


        assertEquals(1, creates);
        assertTrue(firstGoodLogin);
        assertFalse(secondBadLogin);
        assertFalse(thirdBadLogin);
        assertEquals(2, accountBeforeClear.account_login_attempts);
        assertEquals(1, clears);
        assertEquals(0, accountAfterClear.account_login_attempts);
        assertEquals(1, deletes);
    }

    @Test
    public void login_shouldIncrementLoginAttemptsToLessThanMaxLoginAndAccountShouldNotBeLocked_whenFailedToLoginThatMuchTimes() throws AccountManagerException {
        final String email = "badtyper180@login.me";
        final String password = "secretpasswordd#";
        final String badPassword = "alma";
        final boolean locked = false;
        final boolean verified = true;

        final int creates;
        final int deletes;
        final boolean[] logins = new boolean[MAX_LOGIN_ATTEMPTS_FROM_TEST_CONFIG];
        final AccountResultSet account;


        creates = accountManager.create(email, password, locked, verified);
        for (int i = 0; i < MAX_LOGIN_ATTEMPTS_FROM_TEST_CONFIG; i++ ) {
            logins[i] = accountManager.login(email, badPassword);
        }
        account = accountManager.get(email);
        deletes = accountManager.delete(email);
        assertThrows(NoSuchElementException.class, () -> accountManager.get(email));


        assertEquals(1, creates);
        for (boolean login : logins) {
            assertFalse(login);
        }
        assertEquals(MAX_LOGIN_ATTEMPTS_FROM_TEST_CONFIG, account.account_login_attempts);
        assertFalse(account.account_locked);
        assertEquals(1, deletes);
    }

    @Test
    public void login_shouldLockAccount_whenMaxFailedLoginAttemptsExceeded() throws AccountManagerException {
        final String email = "autolock180@login.me";
        final String password = "secretpasswordd#";
        final String badPassword = "alma";
        final boolean locked = false;
        final boolean verified = true;

        final int creates;
        final int deletes;
        final boolean firstLogin;
        final boolean lockedLoginWithGoodPassword;
        final AccountResultSet accountNoBadLogins;
        final AccountResultSet accountShouldBeLocked;


        creates = accountManager.create(email, password, locked, verified);
        firstLogin = accountManager.login(email, password);
        accountNoBadLogins = accountManager.get(email);
        for (int i = 0; i < MAX_LOGIN_ATTEMPTS_FROM_TEST_CONFIG + 20; i++ ) {
            accountManager.login(email, badPassword);
        }
        lockedLoginWithGoodPassword = accountManager.login(email, password);
        accountShouldBeLocked = accountManager.get(email);
        deletes = accountManager.delete(email);
        assertThrows(NoSuchElementException.class, () -> accountManager.get(email));


        assertEquals(1, creates);
        assertTrue(firstLogin);
        assertFalse(accountNoBadLogins.account_locked);
        assertEquals(0, accountNoBadLogins.account_login_attempts);
        assertFalse(lockedLoginWithGoodPassword);
        assertTrue(accountShouldBeLocked.account_locked);
        assertEquals(MAX_LOGIN_ATTEMPTS_FROM_TEST_CONFIG, accountShouldBeLocked.account_login_attempts);
        assertEquals(1, deletes);
    }

    @Test
    public void login_LoginAttemptsShouldResetToZero_afterSuccessfulLogin() throws AccountManagerException {
        final String email = "lastnite@aaa.me";
        final String password = "woho#";
        final String badPassword = "bbbbb";
        final boolean locked = false;
        final boolean verified = true;

        final int creates;
        final int deletes;
        final boolean firstBadLogin;
        final boolean secondBadLogin;
        final boolean thirdBadLogin;
        final boolean fourthGoodLogin;
        final AccountResultSet accountAfterThreeBadLogin;
        final AccountResultSet accountAfterGoodLoginShouldBeReset;


        creates = accountManager.create(email, password, locked, verified);
        firstBadLogin = accountManager.login(email, badPassword);
        secondBadLogin = accountManager.login(email, badPassword);
        thirdBadLogin = accountManager.login(email, badPassword);
        accountAfterThreeBadLogin = accountManager.get(email);

        fourthGoodLogin = accountManager.login(email, password);
        accountAfterGoodLoginShouldBeReset = accountManager.get(email);

        deletes = accountManager.delete(email);
        assertThrows(NoSuchElementException.class, () -> accountManager.get(email));


        assertEquals(1, creates);
        assertFalse(firstBadLogin);
        assertFalse(secondBadLogin);
        assertFalse(thirdBadLogin);
        assertEquals(3, accountAfterThreeBadLogin.account_login_attempts);
        assertTrue(fourthGoodLogin);
        assertEquals(0, accountAfterGoodLoginShouldBeReset.account_login_attempts);
        assertEquals(1, deletes);
    }
}
