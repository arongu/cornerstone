package cornerstone.workflow.app.services.account_service;

import cornerstone.workflow.app.configuration.ConfigReader;
import cornerstone.workflow.app.datasource.DataSourceAccountDB;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class AccountServiceLoginTest {
    private static AccountServiceInterface accountService;

    @BeforeAll
    public static void setSystemProperties() {
        final String dev_files_dir = "../../_dev_files/test_config/";
        final String confPath = Paths.get(dev_files_dir + "app.conf").toAbsolutePath().normalize().toString();
        final String keyPath = Paths.get(dev_files_dir + "key.conf").toAbsolutePath().normalize().toString();

        System.setProperty(ConfigReader.SYSTEM_PROPERTY_KEY__CONF_FILE, confPath);
        System.setProperty(ConfigReader.SYSTEM_PROPERTY_KEY__KEY_FILE, keyPath);

        try {
            final ConfigReader cr = new ConfigReader();
            cr.loadConfig();

            final DataSourceAccountDB ds = new DataSourceAccountDB(cr);
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
    public void login_shouldReturnTrue_whenAccountExistsNotLockedAndVerified() throws AccountServiceException {
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
    public void login_shouldReturnFalse_whenAccountDoesNotExist() throws AccountServiceException {
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
    public void login_shouldReturnFalse_whenAccountNotVerifiedAndNotLocked() throws AccountServiceException {
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
    public void login_shouldReturnFalse_whenAccountLocked() throws AccountServiceException {
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
    public void login_shouldIncrementLoginAttempts_whenLoginFails() throws AccountServiceException {
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
    public void clearLoginAttempts_shouldClearLoginAttempts() throws AccountServiceException {
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

    // TODO cleanup the rest of the TCs ... with
    @Test
    public void login_shouldIncrementLoginAttemptsBy179TimesAccountShouldNotBeLocked_whenLoginFails179Times() throws AccountServiceException {
        final String email = "badtyper180@login.me";
        final String password = "secretpasswordd#";
        final String badPassword = "alma";
        final boolean locked = false;
        final boolean verified = true;

        accountService.create(email, password, locked, verified);
        boolean loggedIn = false;
        for ( int i = 0; i < 179 && !loggedIn; i++ ) {
            loggedIn = accountService.login(email, badPassword);
        }


        final AccountResultSet account = accountService.get(email);
        accountService.delete(email);


        assertEquals(179, account.account_login_attempts);
        assertFalse(account.account_locked);
        assertFalse(loggedIn);
        assertNull(accountService.get(email));
    }

    @Test
    public void login_shouldLockAccountAfter180FailedLoginAttempts() throws AccountServiceException {
        final String email = "autolock180@login.me";
        final String password = "secretpasswordd#";
        final String badPassword = "alma";
        final boolean locked = false;
        final boolean verified = true;

        final boolean firstLogin;
        final boolean after180Login;

        AccountResultSet start;
        AccountResultSet end;


        accountService.create(email, password, locked, verified);
        firstLogin = accountService.login(email, password);
        start = accountService.get(email);

        for ( int i = 0; i < 200; i++ ) {
            accountService.login(email, badPassword);
        }

        after180Login = accountService.login(email, password);
        end = accountService.get(email);

        accountService.delete(email);


        assertTrue(firstLogin);
        assertFalse(start.account_locked);
        assertEquals(0, start.account_login_attempts);
        // after 180 bad attempts
        assertFalse(after180Login);
        assertTrue(end.account_locked);
        assertEquals(180, end.account_login_attempts);
        // cleanup
        assertNull(accountService.get(email));
    }

    @Test
    public void login_afterSuccessfulLoginLoginAttemptsShouldResetToZero() throws AccountServiceException {
        final String email = "lastnite@aaa.me";
        final String password = "woho#";
        final String badPassword = "bbbbb";
        final boolean locked = false;
        final boolean verified = true;

        final boolean firstBadLogin;
        final boolean secondBadLogin;
        final boolean thirdBadLogin;
        final boolean fourthGoodLogin;


        final AccountResultSet accountAfterThreeBadLogin;
        final AccountResultSet accountAfterGoodLoginShouldBeReset;
        final AccountResultSet accountAfterDelete;


        accountService.create(email, password, locked, verified);
        firstBadLogin = accountService.login(email, badPassword);
        secondBadLogin = accountService.login(email, badPassword);
        thirdBadLogin = accountService.login(email, badPassword);
        accountAfterThreeBadLogin = accountService.get(email);

        fourthGoodLogin = accountService.login(email, password);
        accountAfterGoodLoginShouldBeReset = accountService.get(email);

        accountService.delete(email);
        accountAfterDelete = accountService.get(email);


        assertFalse(firstBadLogin);
        assertFalse(secondBadLogin);
        assertFalse(thirdBadLogin);
        assertEquals(3, accountAfterThreeBadLogin.account_login_attempts);
        assertTrue(fourthGoodLogin);
        assertEquals(0, accountAfterGoodLoginShouldBeReset.account_login_attempts);
        assertNull(accountAfterDelete);
    }
}
