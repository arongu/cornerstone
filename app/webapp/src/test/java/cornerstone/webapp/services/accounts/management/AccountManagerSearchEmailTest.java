package cornerstone.webapp.services.accounts.management;

import cornerstone.webapp.configuration.ConfigLoader;
import cornerstone.webapp.datasources.UsersDB;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AccountManagerSearchEmailTest {
    private static AccountManager accountManager;

    @BeforeAll
    public static void setSystemProperties() {
        final String test_files_dir = "../../_test_config/";
        final String keyFile        = Paths.get(test_files_dir + "key.conf").toAbsolutePath().normalize().toString();
        final String confFile       = Paths.get(test_files_dir + "app.conf").toAbsolutePath().normalize().toString();

        try {
            final ConfigLoader configLoader = new ConfigLoader(keyFile, confFile);
            final UsersDB usersDB           = new UsersDB(configLoader);
            accountManager                  = new AccountManagerImpl(usersDB, configLoader);

        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void searchByEmail_wildCardsShouldWork_whenApplied() throws Exception {
        final String normal_zmail     = "normal@zmail.com";
        final String norman_zmail     = "norman@zmail.com";
        final String aabbb_man_com    = "aabbb@man.com";
        final String password         = "password";
        final boolean locked          = false;
        final boolean verified        = true;
        final List<String> wildcard_result1;
        final List<String> wildcard_result2;
        final List<String> wildcard_result3;
        final List<String> exact_result4;
        final List<String> exact_result5;
        final List<String> null_result;
        // prepare
        TestHelper.deleteAccount(accountManager, normal_zmail);
        TestHelper.deleteAccount(accountManager, norman_zmail);
        TestHelper.deleteAccount(accountManager, aabbb_man_com);
        accountManager.create(normal_zmail, password, locked, verified, UserRole.NO_ROLE);
        accountManager.create(norman_zmail, password, locked, verified, UserRole.SUPER);
        accountManager.create(aabbb_man_com, password, locked, verified, UserRole.USER);


        wildcard_result1 = accountManager.searchAccounts("nor%");
        wildcard_result2 = accountManager.searchAccounts("%zmail%");
        wildcard_result3 = accountManager.searchAccounts("%man.com");
        exact_result4    = accountManager.searchAccounts("normal@zmail.com");
        exact_result5    = accountManager.searchAccounts("xxx@bmail.com");
        null_result      = accountManager.searchAccounts(null);


        // wildcard nor%
        assertEquals(2, wildcard_result1.size());
        assertTrue  (wildcard_result1.contains(normal_zmail));
        assertTrue  (wildcard_result1.contains(norman_zmail));
        // "%gmail%"
        assertEquals(2, wildcard_result2.size());
        assertTrue (wildcard_result2.contains(normal_zmail));
        assertTrue (wildcard_result2.contains(norman_zmail));
        // "%man.com"
        assertEquals(1, wildcard_result3.size());
        assertTrue  (wildcard_result3.contains(aabbb_man_com));
        // "normal@zmail.com"
        assertEquals(1, exact_result4.size());
        assertTrue  (exact_result4.contains(normal_zmail));
        // "xxx@bmail.com"
        assertEquals(0, exact_result5.size());
        // null result
        assertEquals(0, null_result.size());
        // cleanup, delete
        accountManager.delete(normal_zmail);
        accountManager.delete(norman_zmail);
        accountManager.delete(aabbb_man_com);
    }
}
