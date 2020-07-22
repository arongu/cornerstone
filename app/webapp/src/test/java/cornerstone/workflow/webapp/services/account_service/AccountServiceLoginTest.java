package cornerstone.workflow.webapp.services.account_service;

import cornerstone.webapp.configuration.ConfigurationLoader;
import cornerstone.webapp.datasources.UsersDB;
import cornerstone.webapp.services.account.administration.AccountAdministration;
import cornerstone.webapp.services.account.administration.AccountAdministrationException;
import cornerstone.webapp.services.account.administration.AccountAdministrationInterface;
import cornerstone.webapp.services.account.administration.AccountResultSet;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

// This is more just a single login test, this heavily uses CRUD methods
// All TCs cleanup after themselves!
// TODO TCs for app_max_login_attempts -- rework!!!
public class AccountServiceLoginTest {
    private static AccountAdministrationInterface accountService;

    @BeforeAll
    public static void setSystemProperties() {
        final String test_files_dir = "../../_test_config/";
        final String keyFile  = Paths.get(test_files_dir + "key.conf").toAbsolutePath().normalize().toString();
        final String confFile = Paths.get(test_files_dir + "app.conf").toAbsolutePath().normalize().toString();

        try {
            final ConfigurationLoader cr = new ConfigurationLoader(keyFile, confFile);
            cr.loadAndDecryptConfig();

            final UsersDB ds = new UsersDB(cr);
            accountService = new AccountAdministration(ds);

        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void login_shouldReturnTrue_whenAccountExistsNotLockedAndVerified() throws AccountAdministrationException {
        final String email = "melchior@login.me";
        final String password = "miciMacko#";
        final boolean locked = false;
        final boolean verified = true;

        final boolean loggedIn;
        final int creates;
        final int deletes;
        final AccountResultSet accountShouldBeDeleted;


        creates = accountService.create(email, password, locked, verified);
        loggedIn = accountService.login(email, password);
        deletes = accountService.delete(email);
        accountShouldBeDeleted = accountService.get(email);


        assertEquals(1, creates);
        assertTrue(loggedIn);
        assertEquals(1, deletes);
        assertNull(accountShouldBeDeleted);
    }

    @Test
    public void login_shouldReturnFalse_whenAccountDoesNotExist() throws AccountAdministrationException {
        final String email = "xxxxx@doesnotexist.xu";
        final String password = "wow";

        final boolean loggedIn;
        final AccountResultSet account;


        loggedIn = accountService.login(email, password);
        account = accountService.get(email);


        assertFalse(loggedIn);
        assertNull(account);
    }

    @Test
    public void login_shouldReturnFalse_whenAccountNotVerifiedAndNotLocked() throws AccountAdministrationException {
        final String email = "casper@login.me";
        final String password = "casper#";
        final boolean locked = false;
        final boolean verified = false;

        final boolean loggedIn;
        final int creates;
        final int deletes;
        final AccountResultSet account;
        final AccountResultSet accountShouldBeDeleted;


        creates = accountService.create(email, password, locked, verified);
        account = accountService.get(email);
        loggedIn = accountService.login(email, password);
        deletes = accountService.delete(email);
        accountShouldBeDeleted = accountService.get(email);


        assertEquals(1, creates);
        assertFalse(loggedIn);
        assertEquals(email, account.email_address);
        assertFalse(account.account_locked);
        assertFalse(account.email_address_verified);
        assertEquals(1, deletes);
        assertNull(accountShouldBeDeleted);
    }

    @Test
    public void login_shouldReturnFalse_whenAccountLocked() throws AccountAdministrationException {
        final String email = "locked@login.me";
        final String password = "locked#";
        final boolean locked = true;
        final boolean verified = true;

        final boolean loggedIn;
        final int creates;
        final int deletes;
        final AccountResultSet account;
        final AccountResultSet accountShouldBeDeleted;


        creates = accountService.create(email, password, locked, verified);
        account = accountService.get(email);
        loggedIn = accountService.login(email, password);
        deletes = accountService.delete(email);
        accountShouldBeDeleted = accountService.get(email);


        assertEquals(1, creates);
        assertTrue(account.account_locked);
        assertFalse(loggedIn);
        assertEquals(1, deletes);
        assertNull(accountShouldBeDeleted);
    }

    @Test
    public void login_shouldIncrementLoginAttempts_whenLoginFails() throws AccountAdministrationException {
        final String email = "badtyper@login.me";
        final String password = "secretpasswordd#";
        final String badPassword = "myBadPassword#";
        final boolean locked = false;
        final boolean verified = true;

        final boolean[] logins = new boolean[3];
        final int creates;
        final int deletes;
        final AccountResultSet accountAfterBadLogins;
        final AccountResultSet accountShouldBeDeleted;


        creates = accountService.create(email, password, locked, verified);
        logins[0] = accountService.login(email, badPassword);
        logins[1] = accountService.login(email, badPassword);
        logins[2] = accountService.login(email, badPassword);
        accountAfterBadLogins = accountService.get(email);
        deletes = accountService.delete(email);
        accountShouldBeDeleted = accountService.get(email);


        assertEquals(1, creates);
        assertEquals(3, accountAfterBadLogins.account_login_attempts);
        assertFalse(logins[0]);
        assertFalse(logins[1]);
        assertFalse(logins[2]);
        assertEquals(1, deletes);
        assertNull(accountShouldBeDeleted);
    }

    @Test
    public void clearLoginAttempts_shouldClearLoginAttempts_whenCalled() throws AccountAdministrationException {
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
        final AccountResultSet accountShouldBeDeleted;


        creates = accountService.create(email, password, locked, verified);
        firstGoodLogin = accountService.login(email, password);
        secondBadLogin = accountService.login(email, badPassword);
        thirdBadLogin = accountService.login(email, badPassword);
        accountBeforeClear = accountService.get(email);
        clears = accountService.clearLoginAttempts(email);
        accountAfterClear = accountService.get(email);
        deletes = accountService.delete(email);
        accountShouldBeDeleted = accountService.get(email);


        assertEquals(1, creates);
        assertTrue(firstGoodLogin);
        assertFalse(secondBadLogin);
        assertFalse(thirdBadLogin);
        assertEquals(2, accountBeforeClear.account_login_attempts);
        assertEquals(1, clears);
        assertEquals(0, accountAfterClear.account_login_attempts);
        assertEquals(1, deletes);
        assertNull(accountShouldBeDeleted);
    }


    @Test
    public void login_shouldIncrementLoginAttemptsBy179TimesAccountShouldNotBeLocked_whenLoginFails179Times() throws AccountAdministrationException {
        final String email = "badtyper180@login.me";
        final String password = "secretpasswordd#";
        final String badPassword = "alma";
        final boolean locked = false;
        final boolean verified = true;

        final int creates;
        final int deletes;
        final boolean[] logins = new boolean[179];
        final AccountResultSet account;
        final AccountResultSet accountShouldBeDeleted;


        creates = accountService.create(email, password, locked, verified);
        for ( int i = 0; i < 179; i++ ) {
            logins[i] = accountService.login(email, badPassword);
        }
        account = accountService.get(email);
        deletes = accountService.delete(email);
        accountShouldBeDeleted = accountService.get(email);


        assertEquals(1, creates);
        for (boolean login : logins) {
            assertFalse(login);
        }
        assertEquals(179, account.account_login_attempts);
        assertFalse(account.account_locked);
        assertEquals(1, deletes);
        assertNull(accountShouldBeDeleted);
    }

    @Test
    public void login_shouldLockAccount_after180FailedLoginAttempts() throws AccountAdministrationException {
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
        final AccountResultSet accountShouldBeDeleted;

        creates = accountService.create(email, password, locked, verified);
        firstLogin = accountService.login(email, password);
        accountNoBadLogins = accountService.get(email);
        for ( int i = 0; i < 200; i++ ) {
            accountService.login(email, badPassword);
        }
        lockedLoginWithGoodPassword = accountService.login(email, password);
        accountShouldBeLocked = accountService.get(email);
        deletes = accountService.delete(email);
        accountShouldBeDeleted = accountService.get(email);


        assertEquals(1, creates);
        assertTrue(firstLogin);
        assertFalse(accountNoBadLogins.account_locked);
        assertEquals(0, accountNoBadLogins.account_login_attempts);
        assertFalse(lockedLoginWithGoodPassword);
        assertTrue(accountShouldBeLocked.account_locked);
        assertEquals(180, accountShouldBeLocked.account_login_attempts);
        assertEquals(1, deletes);
        assertNull(accountShouldBeDeleted);
    }

    @Test
    public void login_LoginAttemptsShouldResetToZero_afterSuccessfulLogin() throws AccountAdministrationException {
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
        final AccountResultSet accountShouldBeDeleted;


        creates = accountService.create(email, password, locked, verified);
        firstBadLogin = accountService.login(email, badPassword);
        secondBadLogin = accountService.login(email, badPassword);
        thirdBadLogin = accountService.login(email, badPassword);
        accountAfterThreeBadLogin = accountService.get(email);

        fourthGoodLogin = accountService.login(email, password);
        accountAfterGoodLoginShouldBeReset = accountService.get(email);

        deletes = accountService.delete(email);
        accountShouldBeDeleted = accountService.get(email);


        assertEquals(1, creates);
        assertFalse(firstBadLogin);
        assertFalse(secondBadLogin);
        assertFalse(thirdBadLogin);
        assertEquals(3, accountAfterThreeBadLogin.account_login_attempts);
        assertTrue(fourthGoodLogin);
        assertEquals(0, accountAfterGoodLoginShouldBeReset.account_login_attempts);
        assertEquals(1, deletes);
        assertNull(accountShouldBeDeleted);
    }
}
