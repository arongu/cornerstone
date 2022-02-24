package cornerstone.webapp.services.accounts.management;

// THIS IS MORE THAN JUST A SINGLE LOGIN TEST, IT HEAVILY RELIES ON CRUD METHODS
// ALL TCS CLEANUP AFTER THEMSELVES FOR DATABASE CONSISTENCY!
public class AccountManagerLoginAndCrudTest {
//    private static AccountManager accountManager;
//    private static int MAX_LOGIN_ATTEMPTS_FROM_TEST_CONFIG;
//
//    @BeforeAll
//    public static void setSystemProperties() {
//        final String test_files_dir = System.getenv("CONFIG_DIR");
//        final String keyFile        = Paths.get(test_files_dir + "key.conf").toAbsolutePath().normalize().toString();
//        final String confFile       = Paths.get(test_files_dir + "app.conf").toAbsolutePath().normalize().toString();
//
//        try {
//            final ConfigLoader configLoader     = new ConfigLoader(keyFile, confFile);
//            final UsersDB usersDB               = new UsersDB(configLoader);
//            accountManager                      = new AccountManagerImpl(usersDB, configLoader);
//            MAX_LOGIN_ATTEMPTS_FROM_TEST_CONFIG = Integer.parseInt(configLoader.getAppProperties().getProperty(APP_ENUM.APP_MAX_LOGIN_ATTEMPTS.key));
//
//        } catch (final IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Test
//    public void login_shouldReturnAccountResultSet_whenAccountExistsNotLockedAndVerified() throws Exception {
//        final String email           = "melchior@login.me";
//        final String password        = "miciMacko#";
//        final boolean locked         = false;
//        final boolean verified       = true;
//        final SYSTEM_ROLE_ENUM accountRole = SYSTEM_ROLE_ENUM.USER;
//        // results
//        final AccountResultSet accountResultSet;
//        final int number_of_accounts_created;
//        final int number_of_accounts_deleted;
//        // remove if exists
//        TestHelper.deleteAccount(accountManager, email);
//
//
//        number_of_accounts_created = accountManager.create(email, password, locked, verified, accountRole);
//        accountResultSet           = accountManager.login(email, password);
//        number_of_accounts_deleted = accountManager.delete(email);
//
//
//        final NoAccountException noAccountException = assertThrows(NoAccountException.class, () -> accountManager.get(email));
//        assertEquals("Account 'melchior@login.me' does not exist.", noAccountException.getMessage());
//        assertEquals(1, number_of_accounts_created);
//        assertEquals(email, accountResultSet.email_address);
//        assertEquals(1, number_of_accounts_deleted);
//    }
//
//    @Test
//    public void login_shouldThrowNoAccountException_whenAccountDoesNotExist() {
//        final String email    = "xxxxx@doesnotexist.xu";
//        final String password = "wow";
//
//        final NoAccountException e = assertThrows(NoAccountException.class, () -> accountManager.login(email, password));
//        assertEquals("Account 'xxxxx@doesnotexist.xu' does not exist.", e.getMessage());
//    }
//
//    @Test
//    public void login_shouldThrowUnverifiedEmailException_whenAccountIsNotVerifiedAndNotLocked() throws Exception {
//        final String email            = "casper@login.me";
//        final String password         = "casper#";
//        final boolean locked          = false;
//        final boolean verified        = false;
//        final SYSTEM_ROLE_ENUM accountRole = SYSTEM_ROLE_ENUM.ADMIN;
//        // results
//        final int number_of_accounts_created;
//        final int number_of_accounts_deleted;
//        final AccountResultSet accountResultSet;
//        // remove if exists
//        TestHelper.deleteAccount(accountManager, email);
//
//
//        number_of_accounts_created       = accountManager.create(email, password, locked, verified, accountRole);
//        accountResultSet                 = accountManager.get(email);
//        final UnverifiedEmailException e = assertThrows(UnverifiedEmailException.class, () -> accountManager.login(email, password));
//
//
//        assertEquals("Account is not verified 'casper@login.me'.", e.getMessage());
//        assertEquals(1, number_of_accounts_created);
//        assertEquals(email, accountResultSet.email_address);
//        assertFalse(accountResultSet.account_locked);
//        assertFalse(accountResultSet.email_address_verified);
//        // cleanup && tiny delete test
//        number_of_accounts_deleted = accountManager.delete(email);
//        assertEquals(1, number_of_accounts_deleted);
//        assertThrows(NoAccountException.class, () -> accountManager.get(email));
//    }
//
//    @Test
//    public void login_shouldThrowLockedException_whenAccountIsLocked() throws Exception {
//        final String email     = "locked@login.me";
//        final String password  = "locked#";
//        final boolean locked   = true;
//        final boolean verified = true;
//        // results
//        final int number_of_accounts_created;
//        final int number_of_accounts_deleted;
//        final AccountResultSet accountResultSet;
//        // remove if exists
//        TestHelper.deleteAccount(accountManager, email);
//
//
//        number_of_accounts_created = accountManager.create(email, password, locked, verified, SYSTEM_ROLE_ENUM.NO_ROLE);
//        accountResultSet           = accountManager.get(email);
//        final LockedException e    = assertThrows(LockedException.class, () -> accountManager.login(email, password));
//
//
//        assertEquals("Account is locked 'locked@login.me'.", e.getMessage());
//        assertEquals(1, number_of_accounts_created);
//        assertTrue(accountResultSet.account_locked);
//        // cleanup && tiny delete test
//        number_of_accounts_deleted = accountManager.delete(email);
//        assertEquals(1, number_of_accounts_deleted);
//        assertThrows(NoAccountException.class, () -> accountManager.get(email)); // verify last account deletion
//    }
//
//    @Test
//    public void login_shouldIncrementLoginAttempts_whenLoginFails() throws Exception {
//        final String email        = "badtyper@login.me";
//        final String password     = "secretpasswordd#";
//        final String bad_password = "myBadPassword#";
//        final boolean locked      = false;
//        final boolean verified    = true;
//        // results
//        final int number_of_accounts_created;
//        final int number_of_accounts_deleted;
//        final AccountResultSet account_after_bad_logins;
//        // remove if exists
//        TestHelper.deleteAccount(accountManager, email);
//
//
//        number_of_accounts_created = accountManager.create(email, password, locked, verified, SYSTEM_ROLE_ENUM.USER);
//        assertThrows(BadPasswordException.class, () -> accountManager.login(email, bad_password));
//        assertThrows(BadPasswordException.class, () -> accountManager.login(email, bad_password));
//        final BadPasswordException e = assertThrows(BadPasswordException.class, () -> accountManager.login(email, bad_password));
//        account_after_bad_logins   = accountManager.get(email);
//        number_of_accounts_deleted = accountManager.delete(email);
//
//
//        assertEquals("Bad password provided for 'badtyper@login.me'.", e.getMessage());
//        assertEquals(1, number_of_accounts_created);
//        assertEquals(3, account_after_bad_logins.account_login_attempts);
//        assertEquals(1, number_of_accounts_deleted);
//        assertThrows(NoAccountException.class, () -> accountManager.get(email)); // verify last delete
//    }
//
//    @Test
//    public void clearLoginAttempts_shouldClearLoginAttempts_whenCalled() throws Exception {
//        final String email        = "badtyper@login.me";
//        final String password     = "secretpasswordd#";
//        final String bad_password = "myBadPassword#";
//        final boolean locked      = false;
//        final boolean verified    = true;
//        // results
//        final int number_of_accounts_created;
//        final int number_of_accounts_cleared;
//        final int number_of_accounts_deleted;
//        final AccountResultSet account_frist_login;
//        final AccountResultSet account_before_clear;
//        final AccountResultSet account_after_clear;
//        // delete if exists
//        TestHelper.deleteAccount(accountManager, email);
//
//
//        number_of_accounts_created        = accountManager.create(email, password, locked, verified, SYSTEM_ROLE_ENUM.USER);
//        account_frist_login               = accountManager.login(email, password);
//        assertThrows(BadPasswordException.class, () -> accountManager.login(email, bad_password));
//        assertThrows(BadPasswordException.class, () -> accountManager.login(email, bad_password));
//        account_before_clear              = accountManager.get(email);
//        number_of_accounts_cleared        = accountManager.clearLoginAttempts(email);
//        account_after_clear               = accountManager.get(email);
//        number_of_accounts_deleted        = accountManager.delete(email);
//
//
//        assertEquals(1, number_of_accounts_created);
//        assertEquals(0, account_frist_login.account_login_attempts);
//        assertEquals(2, account_before_clear.account_login_attempts);
//        assertEquals(1, number_of_accounts_cleared);
//        assertEquals(0, account_after_clear.account_login_attempts);
//        assertEquals(1, number_of_accounts_deleted);
//        final NoAccountException e = assertThrows(NoAccountException.class, () -> accountManager.get(email)); // verify last delete
//        assertEquals("Account 'badtyper@login.me' does not exist.", e.getMessage());
//    }
//
//    @Test
//    public void login_shouldIncrementLoginAttemptsToLessThanMaxLoginAndAccountShouldNotBeLocked_whenFailedToLoginThatManyTimes() throws Exception {
//        final String email        = "badtyper180@login.me";
//        final String password     = "secretpasswordd#";
//        final String bad_password = "alma";
//        final boolean locked      = false;
//        final boolean verified    = true;
//        // results
//        final int number_of_accounts_created;
//        final int number_of_accounts_deleted;
//        boolean login_locked_first_time;
//        int login_attempts_first_time;
//        // delete if exists
//        TestHelper.deleteAccount(accountManager, email);
//
//
//        number_of_accounts_created = accountManager.create(email, password, locked, verified, SYSTEM_ROLE_ENUM.USER);
//        for (int i = 0; i < MAX_LOGIN_ATTEMPTS_FROM_TEST_CONFIG; i++) {
//            assertThrows(BadPasswordException.class, () -> accountManager.login(email, bad_password));
//        }
//        AccountResultSet accountResultSet = accountManager.get(email);
//        login_attempts_first_time         = accountResultSet.account_login_attempts;
//        login_locked_first_time           = accountResultSet.account_locked;
//
//
//        assertEquals(1, number_of_accounts_created);
//        assertFalse(login_locked_first_time);
//        assertEquals(MAX_LOGIN_ATTEMPTS_FROM_TEST_CONFIG, login_attempts_first_time);
//        // cleanup && tiny delete test
//        number_of_accounts_deleted = accountManager.delete(email);
//        assertEquals(1, number_of_accounts_deleted);
//        final NoAccountException e = assertThrows(NoAccountException.class, () -> accountManager.get(email));
//        assertEquals("Account 'badtyper180@login.me' does not exist.", e.getMessage());
//    }
//
//    @Test
//    public void login_shouldLockAccount_whenMaxFailedLoginAttemptsExceeded() throws Exception {
//        final String email        = "autolock180@login.me";
//        final String password     = "secretpasswordd#";
//        final String bad_password = "alma";
//        final boolean locked      = false;
//        final boolean verified    = true;
//        // results
//        final int number_of_accounts_created;
//        final int number_of_accounts_deleted;
//        final AccountResultSet result_first_login_good_password;
//        final AccountResultSet result_after_lock;
//        // delete if exists
//        TestHelper.deleteAccount(accountManager, email);
//
//
//        number_of_accounts_created        = accountManager.create(email, password, locked, verified, SYSTEM_ROLE_ENUM.USER);
//        result_first_login_good_password  = accountManager.login(email, password);
//        for (int i = 0; i <= MAX_LOGIN_ATTEMPTS_FROM_TEST_CONFIG; i++) {
//            final BadPasswordException e = assertThrows(BadPasswordException.class, () -> accountManager.login(email, bad_password));
//            assertEquals("Bad password provided for 'autolock180@login.me'.", e.getMessage());
//        }
//        for (int i = 0; i < 20; i++) {
//            final LockedException e = assertThrows(LockedException.class, () -> accountManager.login(email, bad_password));
//            assertEquals("Account is locked 'autolock180@login.me'." , e.getMessage());
//        }
//        result_after_lock = accountManager.get(email);
//
//
//        assertEquals(1, number_of_accounts_created);
//        assertEquals(email, result_first_login_good_password.email_address);
//        assertEquals(0, result_first_login_good_password.account_login_attempts);
//        assertTrue(result_after_lock.account_locked);
//        assertEquals(MAX_LOGIN_ATTEMPTS_FROM_TEST_CONFIG, result_after_lock.account_login_attempts);
//        // cleanup && tiny delete test
//        number_of_accounts_deleted = accountManager.delete(email);
//        final NoAccountException noAccountException = assertThrows(NoAccountException.class, () -> accountManager.get(email));
//        assertEquals(1, number_of_accounts_deleted);
//        assertEquals("Account 'autolock180@login.me' does not exist.", noAccountException.getMessage());
//    }
//
//    @Test
//    public void login_loginAttemptsShouldResetToZero_whenLoginIsSuccessful() throws Exception {
//        final String email        = "lastnite@aaa.me";
//        final String password     = "woho#";
//        final String bad_password = "bbbbb";
//        final boolean locked      = false;
//        final boolean verified    = true;
//        // results
//        final int number_of_accounts_created;
//        final int number_of_accounts_deleted;
//        final AccountResultSet account_after_three_bad_logins;
//        final AccountResultSet account_after_fourth_login_good_password;
//        final AccountResultSet account_after_fourth_login_and_clear;
//        // delete if exists
//        TestHelper.deleteAccount(accountManager, email);
//
//
//        number_of_accounts_created               = accountManager.create(email, password, locked, verified, SYSTEM_ROLE_ENUM.USER);
//        assertThrows(BadPasswordException.class, () -> accountManager.login(email, bad_password));
//        assertThrows(BadPasswordException.class, () -> accountManager.login(email, bad_password));
//        assertThrows(BadPasswordException.class, () -> accountManager.login(email, bad_password));
//        account_after_three_bad_logins           = accountManager.get(email);
//        account_after_fourth_login_good_password = accountManager.login(email, password);
//        account_after_fourth_login_and_clear     = accountManager.get(email);
//
//
//        assertEquals(1, number_of_accounts_created);
//        assertEquals(3, account_after_three_bad_logins.account_login_attempts);
//        assertEquals(email, account_after_fourth_login_good_password.email_address);
//        assertEquals(3, account_after_fourth_login_good_password.account_login_attempts);
//        assertEquals(0, account_after_fourth_login_and_clear.account_login_attempts);
//        // cleanup && tiny delete test
//        number_of_accounts_deleted = accountManager.delete(email);
//        assertEquals(1, number_of_accounts_deleted);
//        final NoAccountException e = assertThrows(NoAccountException.class, () -> accountManager.get(email)); // verify last delete
//        assertEquals("Account 'lastnite@aaa.me' does not exist.", e.getMessage());
//    }
}
