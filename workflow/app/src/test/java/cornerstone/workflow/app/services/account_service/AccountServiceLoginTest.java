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
    public void login_shouldReturnTrue_whenAccountExistsNotLockedAndVerified() throws AccountServiceException {
        final String email = "melchior@login.me";
        final String password = "miciMacko#";
        final boolean locked = false;
        final boolean verified = true;

        accountService.create(email, password, locked, verified);
        final boolean loggedIn = accountService.login(email, password);
        accountService.delete(email);

        assertTrue(loggedIn);
        assertNull(accountService.get(email));
    }

    @Test
    public void login_shouldReturnFalse_whenAccountDoesNotExist() throws AccountServiceException {
        final String email = "xxxxx@doesnotexist.xu";
        final String password = "wow";

        final boolean loggedIn = accountService.login(email, password);

        assertFalse(loggedIn);
        assertNull(accountService.get(email));
    }

    @Test
    public void login_shouldReturnFalse_whenAccountExistsNotLockedAndNotVerified() throws AccountServiceException {
        final String email = "casper@login.me";
        final String password = "casper#";
        final boolean locked = false;
        final boolean verified = false;

        accountService.create(email, password, locked, verified);
        final boolean loggedIn = accountService.login(email, password);
        accountService.delete(email);

        assertFalse(loggedIn);
        assertNull(accountService.get(email));
    }

    @Test
    public void login_shouldReturnFalse_whenAccountExistsAndLocked() throws AccountServiceException {
        final String email = "locked@login.me";
        final String password = "locked#";
        final boolean locked = true;
        final boolean verified = true;

        accountService.create(email, password, locked, verified);
        final boolean loggedIn = accountService.login(email, password);
        accountService.delete(email);

        assertFalse(loggedIn);
        assertNull(accountService.get(email));
    }

    @Test
    public void login_shouldIncrementLoginAttempts_whenLoginFails() throws AccountServiceException {
        final String email = "badtyper@login.me";
        final String password = "secretpasswordd#";
        final String badPassword = "myBadPassword#";
        final boolean locked = false;
        final boolean verified = true;

        accountService.create(email, password, locked, verified);
        final boolean loggedIn = accountService.login(email, badPassword);
        final AccountResultSet account = accountService.get(email);
        accountService.delete(email);

        assertEquals(1, account.account_login_attempts);
        assertFalse(loggedIn);
        assertNull(accountService.get(email));
    }

    @Test
    public void clearLoginAttempts_shouldClearLoginAttempts() throws AccountServiceException {
        final String email = "badtyper@login.me";
        final String password = "secretpasswordd#";
        final String badPassword = "myBadPassword#";
        final boolean locked = false;
        final boolean verified = true;

        final AccountResultSet beforeClear;
        final AccountResultSet afterClear;

        boolean firstGoodLogin;
        boolean secondBadLogin;
        boolean thirdBadLogin;

        accountService.create(email, password, locked, verified);
        firstGoodLogin = accountService.login(email, password);
        secondBadLogin = accountService.login(email, badPassword);
        thirdBadLogin = accountService.login(email, badPassword);

        beforeClear = accountService.get(email);
        accountService.clearLoginAttempts(email);
        afterClear = accountService.get(email);
        accountService.delete(email);

        assertTrue(firstGoodLogin);
        assertFalse(secondBadLogin);
        assertFalse(thirdBadLogin);
        assertEquals(2, beforeClear.account_login_attempts);
        assertEquals(0, afterClear.account_login_attempts);
        assertNull(accountService.get(email));
    }

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

    // TODO after successful login, login attempts should go away !!!!
    //  + needs feature dev
}
