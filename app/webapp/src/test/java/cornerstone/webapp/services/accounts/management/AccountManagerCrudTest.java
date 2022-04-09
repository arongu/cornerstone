package cornerstone.webapp.services.accounts.management;

import cornerstone.webapp.configuration.ConfigLoader;
import cornerstone.webapp.datasources.AccountsDB;
import cornerstone.webapp.services.accounts.management.exceptions.account.common.ParameterNotSetException;
import cornerstone.webapp.services.accounts.management.exceptions.account.single.AccountNotExistsException;
import cornerstone.webapp.services.accounts.management.exceptions.account.single.AccountRetrievalException;
import cornerstone.webapp.services.accounts.management.exceptions.account.single.CreationException;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AccountManagerCrudTest {
    private static AccountManager accountManager;
    private static final String test_email = "aron3@xmail.com";

    @BeforeAll
    public static void setSystemProperties() throws IOException {
        final String test_files_dir = System.getenv("CONFIG_DIR");
        final String keyFile        = Paths.get(test_files_dir + "key.conf").toAbsolutePath().normalize().toString();
        final String confFile       = Paths.get(test_files_dir + "app.conf").toAbsolutePath().normalize().toString();
        final AccountsDB accountsDB = new AccountsDB(new ConfigLoader(keyFile, confFile));
        accountManager              = new AccountManagerImpl(accountsDB);
    }

    // -------------------------------------------- TCs --------------------------------------------
    @Test
    @Order(1)
    public void getAccount_shouldThrowException_whenAccountDoesNotExist() throws AccountRetrievalException, ParameterNotSetException, AccountNotExistsException {
        final AccountNotExistsException e = assertThrows(AccountNotExistsException.class, () -> accountManager.get("aron3@xmail.com"));
        assertEquals("Account 'aron3@xmail.com' does not exist.", e.getMessage());
    }

    @Test
    @Order(2)
    public void createAccount_shouldCreateAccount_whenAccountDoesNotExistYet() throws ParameterNotSetException, CreationException {
        final UUID accountId      = UUID.randomUUID();
        final String email        = "aron3@xmail.com";
        final String passwordHash = "hash3";


        int n = accountManager.createAccount(accountId, email, passwordHash);


        assertEquals(1, n);
    }
//
//    @Test
//    @Order(10)
//    public void x() throws ParameterNotSetException {
//        final UUID groupId = UUID.randomUUID();
//        final UUID ownerId = UUID.randomUUID();
//        final String groupName = "group2";
//        final String notes = "notes";
//        int maxUsers = 15;
//
//        int n = accountManager.createGroup(groupId, UUID.fromString("dab84eb7-3a72-4e51-86b9-3b3d900c05d1"), groupName, notes, maxUsers);
//        System.out.println(n);
//    }
//
//    @Test
//    @Order(20)
//    public void x2() throws ParameterNotSetException {
//        final UUID groupId = UUID.randomUUID();
//        final UUID ownerId = UUID.randomUUID();
//
//        int n = accountManager.createSubAccount(groupId, UUID.randomUUID(), "xadded_to_group_email", "2xxx");
//        System.out.println(n);
//    }

//    @Test
//    @Order(0)
//    public void t00_get_shouldThrowAccountDoesNotExistException_whenAccountDoesNotExist() {
//        final NoAccountException e = assertThrows(NoAccountException.class, () -> accountManager.get("thereisnoway@suchemailexist.net"));
//        assertEquals("Account 'thereisnoway@suchemailexist.net' does not exist.", e.getMessage());
//    }

//    @Test
//    @Order(10)
//    public void t01a_create_and_get_shouldCreateOneAccount_whenAccountDoesNotExist() throws Exception {
//        final SYSTEM_ROLE_ENUM        systemRole       = SYSTEM_ROLE_ENUM.USER;
//        final UUID                    accountId        = UUID.randomUUID();
//        final ACCOUNT_TYPE_ENUM       accountType      = ACCOUNT_TYPE_ENUM.SINGLE_ACCOUNT;
//        final String                  email            = "almafa@gmail.com";
//        final String                  password         = "password";
//        final boolean                 locked           = false;
//        final String                  lockReason       = null;
//        final boolean                 verified         = true;
//        final MULTI_ACCOUNT_ROLE_ENUM multiAccountRole = MULTI_ACCOUNT_ROLE_ENUM.NOT_APPLICABLE;
//        final UUID                    parantAccountId  = null;
//        // time
//        final Timestamp ts = new Timestamp(System.currentTimeMillis());
//        // results
//        final int number_of_accounts_created;
//        final AccountResultSet received_account;
//        // delete if exists
//        TestHelper.deleteAccount(accountManager, email);
//        number_of_accounts_created = accountManager.create(systemRole, accountId, accountType, email, password, locked, lockReason, verified, multiAccountRole, parantAccountId);
////        received_account           = accountManager.get(email);
////
////
////        // Knowable value tests
////        assertEquals(1, number_of_accounts_created);
////        assertEquals(email, received_account.email_address);
////        assertEquals(locked, received_account.account_locked);
////        assertNull(received_account.account_lock_reason);
////        assertEquals(verified, received_account.email_address_verified);
////        assertEquals(0, received_account.account_login_attempts);
////        assertEquals(role.getId(), received_account.role_id);
////        // Dynamic value tests
////        assertTrue(received_account.account_id > 0);
////        assertEquals(received_account.email_address_ts, received_account.account_registration_ts); // happens same time
////        assertEquals(received_account.email_address_ts, received_account.password_hash_ts);        // happens same time
////        assertTrue(ts.before(received_account.account_registration_ts));
////        assertTrue(ts.before(received_account.account_locked_ts));
////        assertTrue(ts.before(received_account.password_hash_ts));
//    }

//    @Test
//    @Order(11)
//    public void t01b_create_shouldThrowAccountCreationDuplicateException_whenAccountAlreadyExists() {
//        final CreationDuplicateException e = assertThrows(CreationDuplicateException.class, () -> {
//            final String email            = "almafa@gmail.com";
//            final String password         = "password";
//            final boolean locked          = false;
//            final boolean verified        = true;
//            final SYSTEM_ROLE_ENUM accountRole = SYSTEM_ROLE_ENUM.USER;
//
//            //accountManager.create(email, password, locked, verified, accountRole);
//        });
//        assertEquals("Failed to create 'almafa@gmail.com' (already exists).", e.getMessage());
//    }

//    @Test
//    @Order(20)
//    public void t02_delete_previousAccountShouldBeDeleted() throws Exception {
//        final String email = "almafa@gmail.com";
//        final int number_of_accounts_deleted;
//
//
//        number_of_accounts_deleted = accountManager.delete("almafa@gmail.com");
//
//
//        final NoAccountException e = assertThrows(NoAccountException.class, () -> accountManager.get(email));
//        assertEquals(1, number_of_accounts_deleted);
//        assertEquals("Account 'almafa@gmail.com' does not exist." , e.getMessage());
//    }
//
//    @Test
//    @Order(30)
//    public void t03_create_anotherAccountShouldBeCreated() throws Exception {
//        final String email            = "crud_tests@x-mail.com";
//        final String password         = "password";
//        final boolean locked          = false;
//        final boolean verified        = true;
//        final SYSTEM_ROLE_ENUM accountRole = SYSTEM_ROLE_ENUM.SUPER;
//        // results
//        final int number_of_accounts_created;
//        final AccountResultSet received_account;
//
//
//        number_of_accounts_created = accountManager.create(email, password, locked, verified, accountRole);
//        received_account           = accountManager.get(email);
//
//
//        assertEquals(1, number_of_accounts_created);
//        assertEquals(email, received_account.email_address);
//        assertEquals(locked, received_account.account_locked);
//        assertEquals(accountRole.getId(), received_account.role_id);
//        assertEquals(verified, received_account.email_address_verified);
//        assertEquals(received_account.password_hash, Crypt.crypt(password, received_account.password_hash));
//    }
//
//    @Test
//    @Order(40)
//    public void t04_setNewEmailAddress_shouldSetNewEmailForPreviouslyCreatedAccount() throws Exception {
//        final String email     = "crud_tests@x-mail.com";
//        final String new_email = "my_new_crud_tests_mail@yahoo.com";
//        // results
//        final int number_of_email_changes;
//        final AccountResultSet beforeEmailChange;
//        final AccountResultSet afterEmailChange;
//
//
//        beforeEmailChange       = accountManager.get(email);
//        number_of_email_changes = accountManager.setEmail(email, new_email);
//        afterEmailChange        = accountManager.get(new_email);
//
//
//        assertEquals(1, number_of_email_changes);
//        assertThrows(NoAccountException.class, () -> accountManager.get(email)); // old email should throw exception
//        assertEquals(new_email, afterEmailChange.email_address);                           // get new email account should return account
//        assertEquals(beforeEmailChange.account_id, afterEmailChange.account_id);           // account id should be same for the new email
//    }
//
//    @Test
//    @Order(50)
//    public void t05_lock_shouldLockAccount() throws Exception {
//        final String email_address = "my_new_crud_tests_mail@yahoo.com";
//        final String reason        = "naughty";
//        // results
//        final int locks;
//        final AccountResultSet beforeLock;
//        final AccountResultSet afterLock;
//
//
//        beforeLock = accountManager.get(email_address);
//        locks      = accountManager.lock(email_address, reason);
//        afterLock  = accountManager.get(email_address);
//
//
//        assertEquals(email_address, beforeLock.email_address);
//        assertFalse(beforeLock.account_locked);
//        assertNull(beforeLock.account_lock_reason);
//        // only one account should be locked
//        assertEquals(1, locks);
//        // after lock
//        assertEquals(email_address, afterLock.email_address);
//        assertEquals(reason, afterLock.account_lock_reason);
//        assertTrue(afterLock.account_locked);
//    }
//
//    @Test
//    @Order(60)
//    public void t06_unlock_shouldUnlockPreviouslyLockedAccount() throws Exception {
//        final String email_address = "my_new_crud_tests_mail@yahoo.com";
//        final String reason        = "naughty";
//        // results
//        final int unlocks;
//        final AccountResultSet beforeUnlock;
//        final AccountResultSet afterUnlock;
//
//
//        beforeUnlock = accountManager.get(email_address);
//        unlocks      = accountManager.unlock(email_address);
//        afterUnlock  = accountManager.get(email_address);
//
//
//        assertEquals(email_address, beforeUnlock.email_address);
//        assertTrue(beforeUnlock.account_locked);
//        assertEquals(reason, beforeUnlock.account_lock_reason);
//        assertEquals(1, unlocks);
//        assertFalse(afterUnlock.account_locked);
//        assertNull(afterUnlock.account_lock_reason);
//    }
//
//    @Test
//    @Order(70)
//    public void t07_changePassword_shouldChangePasswordOfAccount() throws Exception {
//        final String email       = "my_new_crud_tests_mail@yahoo.com";
//        final String password    = "password";
//        final String newPassword = "almafa1234#";
//        // results
//        final int number_of_password_sets;
//        final AccountResultSet beforePasswordChange;
//        final AccountResultSet afterPasswordChange;
//
//
//        beforePasswordChange    = accountManager.get(email);
//        number_of_password_sets = accountManager.setPassword(email, newPassword);
//        afterPasswordChange     = accountManager.get(email);
//
//
//        assertEquals(1, number_of_password_sets);
//        assertEquals(beforePasswordChange.password_hash, Crypt.crypt(password, beforePasswordChange.password_hash));
//        assertEquals(afterPasswordChange.password_hash, Crypt.crypt(newPassword, afterPasswordChange.password_hash));
//    }
//
//    @Test
//    @Order(80)
//    public void t08_setRole_shouldUpdateRole() throws Exception {
//        final String email = "my_new_crud_tests_mail@yahoo.com";
//        // results
//        int number_of_updates = 0;
//        final AccountResultSet beforeUpdate;
//        final AccountResultSet afterUpdate1;
//        final AccountResultSet afterUpdate2;
//        final AccountResultSet afterUpdate3;
//        final AccountResultSet afterUpdate4;
//
//
//        beforeUpdate       = accountManager.get(email);
//        number_of_updates += accountManager.setRole(email, SYSTEM_ROLE_ENUM.NO_ROLE);
//        afterUpdate1       = accountManager.get(email);
//        number_of_updates += accountManager.setRole(email, SYSTEM_ROLE_ENUM.USER);
//        afterUpdate2       = accountManager.get(email);
//        number_of_updates += accountManager.setRole(email, SYSTEM_ROLE_ENUM.SUPER);
//        afterUpdate3       = accountManager.get(email);
//        number_of_updates += accountManager.setRole(email, SYSTEM_ROLE_ENUM.ADMIN);
//        afterUpdate4       = accountManager.get(email);
//
//
//        assertEquals(4, number_of_updates);
//        assertEquals(SYSTEM_ROLE_ENUM.SUPER.getId(), beforeUpdate.role_id);
//        assertEquals(SYSTEM_ROLE_ENUM.NO_ROLE.getId(), afterUpdate1.role_id);
//        assertEquals(SYSTEM_ROLE_ENUM.USER.getId(), afterUpdate2.role_id);
//        assertEquals(SYSTEM_ROLE_ENUM.SUPER.getId(), afterUpdate3.role_id);
//        assertEquals(SYSTEM_ROLE_ENUM.ADMIN.getId(), afterUpdate4.role_id);
//    }
//
//    @Test
//    @Order(90)
//    public void t09_delete_shouldDeleteAccount() throws Exception {
//        final String email = "my_new_crud_tests_mail@yahoo.com";
//        final int number_of_deletes;
//
//
//        number_of_deletes = accountManager.delete(email);
//
//
//        assertEquals(1, number_of_deletes);
//        final NoAccountException e = assertThrows(NoAccountException.class, () -> accountManager.get(email));
//        assertEquals("Account 'my_new_crud_tests_mail@yahoo.com' does not exist.", e.getMessage());
//    }
//
//    @Test
//    @Order(100)
//    public void t10_create_shouldThrowNoAccountException_whenCreateHasNullAsValue() throws Exception {
//        final String email = "blabla@xxx.com";
//
//
//        assertThrows(CreationNullException.class, () -> accountManager.create(email, null, false, true, SYSTEM_ROLE_ENUM.USER));
//        assertThrows(CreationNullException.class, () -> accountManager.create(null, "password", false, true, SYSTEM_ROLE_ENUM.USER));
//        assertThrows(CreationNullException.class, () -> accountManager.create(email, "password", false, true, null));
//        assertThrows(CreationNullException.class, () -> accountManager.create(null, null, false, true, SYSTEM_ROLE_ENUM.SUPER));
//        assertThrows(CreationNullException.class, () -> accountManager.create(null, null, false, true, null));
//
//
//        final NoAccountException e = assertThrows(NoAccountException.class, () -> accountManager.get(email));
//        assertEquals("Account 'blabla@xxx.com' does not exist.", e.getMessage());
//    }
}
