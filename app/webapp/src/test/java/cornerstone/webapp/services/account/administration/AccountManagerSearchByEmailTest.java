package cornerstone.webapp.services.account.administration;

import cornerstone.webapp.config.ConfigLoader;
import cornerstone.webapp.datasources.UsersDB;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AccountManagerSearchByEmailTest {
    private static AccountManager accountManager;

    @BeforeAll
    public static void setSystemProperties() {
        final String test_files_dir = "../../_test_config/";
        final String keyFile        = Paths.get(test_files_dir + "key.conf").toAbsolutePath().normalize().toString();
        final String confFile       = Paths.get(test_files_dir + "app.conf").toAbsolutePath().normalize().toString();

        try {
            final ConfigLoader configLoader = new ConfigLoader(keyFile, confFile);
            configLoader.loadAndDecryptConfig();

            final UsersDB usersDB               = new UsersDB(configLoader);
            accountManager                      = new AccountManagerImpl(usersDB, configLoader);

        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void searchByEmail_wildCardsShouldWork_whenApplied() throws Exception {
        final String email     = "searchme@gmail.com";
        final String email2    = "aaacccddddeeee@gmail.com";
        final String password  = "password";
        final boolean locked   = false;
        final boolean verified = true;
        final List<String> wildcard_result1;
        final List<String> wildcard_result2;
        final List<String> exact_result3;
        final List<String> exact_result4;


        TestHelper.deleteAccount(accountManager, email);
        TestHelper.deleteAccount(accountManager, email2);
        accountManager.create(email, password, locked, verified);
        accountManager.create(email2, password, locked, verified);
        wildcard_result1 = accountManager.searchByEmail("search%");
        wildcard_result2 = accountManager.searchByEmail("%c%");
        exact_result3 = accountManager.searchByEmail("searchme@gmail.com");
        exact_result4 = accountManager.searchByEmail("searchme@gmail.co");


        assertTrue(wildcard_result1.contains(email));
        assertFalse(wildcard_result1.contains(email2));
        assertTrue(wildcard_result2.contains(email));
        assertTrue(wildcard_result2.contains(email2));
        assertEquals(1, exact_result3.size());
        assertEquals(0, exact_result4.size());
        // cleanup, delete
        accountManager.delete(email);
        accountManager.delete(email2);
    }
}
