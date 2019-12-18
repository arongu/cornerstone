package cornerstone.workflow.app.services.account_service;

import cornerstone.workflow.app.configuration.ConfigurationProvider;
import cornerstone.workflow.app.datasource.AccountDB;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class AccountServiceImplTest {
    private static final String confPath = "/home/aron/.corner/app.conf";
    private static final String keyPath = "/home/aron/.corner/key.conf";
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
    public void test() throws AccountServiceException {
        final AccountDB accountDB = new AccountDB(configurationProvider);
        final AccountService accountService = new AccountServiceImpl(accountDB);

//        accountService.deleteAccount("asd");
        //accountService.createAccount("asd", "sad");
        accountService.setAccountPassword("asd", "almafa");
    }

//    @Test
//    public void testa(){
//
//    }

}
