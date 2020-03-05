package cornerstone.workflow.app.services.account_service;

import cornerstone.workflow.app.configuration.ConfigurationProvider;
import cornerstone.workflow.app.datasource.DataSourceAccountDB;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

// TODO fix tc

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AccountServiceImplTest {

    private static final String dev_files_dir = "../../_dev_files/test_config/";
    private static final String confPath  = Paths.get(dev_files_dir + "app.conf").toAbsolutePath().normalize().toString();
    private static final String keyPath = Paths.get(dev_files_dir + "key.conf").toAbsolutePath().normalize().toString();

    private static ConfigurationProvider configurationProvider;

    @BeforeAll
    public static void init() {
        System.setProperty(ConfigurationProvider.SYSTEM_PROPERTY_CONF_FILE, confPath);
        System.setProperty(ConfigurationProvider.SYSTEM_PROPERTY_KEY_FILE, keyPath);

        try {
            configurationProvider = new ConfigurationProvider();
            configurationProvider.loadConfig();

        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    @AfterAll
    public static void cleanUp() {
        System.clearProperty(ConfigurationProvider.SYSTEM_PROPERTY_CONF_FILE);
        System.clearProperty(ConfigurationProvider.SYSTEM_PROPERTY_KEY_FILE);
    }

    @Test
    @Order(1)
    public void getAccount_shouldReturnNull_whenAccountDoesNotExist() throws AccountServiceException {
        final DataSourceAccountDB ds_account_db = new DataSourceAccountDB(configurationProvider);
        final AccountService accountService = new AccountServiceImpl(ds_account_db);

        assertNull(accountService.getAccount("nosuch@mail.com"));
    }



    @Test
    @Order(2)
    public void createAccount_shouldCreateOneAccount_whenAccountDoesNotExist() throws AccountServiceException {
        final String EMAIL_ADDRESS = "almafa@gmail.com";
        final String PASSWORD = "password";
        final boolean LOCKED = false;

        final DataSourceAccountDB ds = new DataSourceAccountDB(configurationProvider);
        final AccountService srv = new AccountServiceImpl(ds);


        final int n = srv.createAccount(EMAIL_ADDRESS, PASSWORD, LOCKED);


        final AccountResultSetDto accountResultSetDto = srv.getAccount(EMAIL_ADDRESS);
        assertEquals(1, n);
        assertEquals(EMAIL_ADDRESS, accountResultSetDto.get_email_address());
        assertEquals(LOCKED, accountResultSetDto.get_account_locked());
    }

    @Test
    @Order(3)
    public void deleteAccount_shouldDeleteAccount_whenCalled() throws AccountServiceException {
        final DataSourceAccountDB ds = new DataSourceAccountDB(configurationProvider);
        final AccountService srv = new AccountServiceImpl(ds);

        final int n = srv.deleteAccount("almafa@gmail.com");

        assertNull(srv.getAccount("almafa@gmail.com"));
        assertEquals(1, n);
    }


    /*@Test
    public void crud_tests() throws AccountServiceException {
        final String email_address = "crud_tests@x-mail.com";
        final String email_address_changed = "my_new_crud_tests_mail@yahoo.com";
        final String password = "password";
        final String reason = "disable reason";


        final AccountDB accountDB = new AccountDB(configurationProvider);
        final AccountService accountService = new AccountServiceImpl(accountDB);
        

        // Phase #2 change email address
        int id = dto.get_account_id();
        accountService.setAccountEmailAddress(email_address, email_address_changed);    // change email address
        dto = accountService.getAccount(email_address_changed);                         // get account

        // Should return null, since email address changed !
        assertNull(accountService.getAccount(email_address));                           // should return null

        // Id should be the same, since the account id is unchanged, only the address
        assertEquals(id, dto.get_account_id());


        // Phase #3 disable account
        accountService.unlockAccount(email_address_changed);                            // disable account
        dto = accountService.getAccount(email_address_changed);                         // get account

        assertFalse(dto.get_account_locked());                                          // should return false
        assertEquals(reason, dto.get_account_lock_reason());                            // should match with reason


        // Phase #4 re-enable account
        accountService.lockAccount(email_address_changed, "lock reason");       // enable account
        dto = accountService.getAccount(email_address_changed);                         // get account

        assertTrue(dto.get_account_locked());                                           // should be true


        // Phase #5 password change
        String originalPassword = dto.get_password_hash();                              // store original password hash
        accountService.setAccountPassword(email_address_changed, "newpassword");
        dto = accountService.getAccount(email_address_changed);


        assertNotEquals(originalPassword, dto.get_password_hash());


        // Phase #6 cleanup
        accountService.deleteAccount(email_address_changed);                            // delete account
        assertNull(accountService.getAccount(email_address_changed));                   // cleanup and delete test
    }**/
}
