package cornerstone.webapp.configuration;

import cornerstone.webapp.configuration.enums.APP_ENUM;
import cornerstone.webapp.configuration.enums.DB_USERS_ENUM;
import cornerstone.webapp.configuration.enums.DB_WORK_ENUM;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

public class ConfigSorterTest {
    // Check passwords are hidden
    // TODO https://www.baeldung.com/junit-asserting-logs

    @Test
    public void sortProperties_shouldThrowException_whenAllTheRequiredKeysAreMissing() throws ConfigSorterException {
        final Properties properties     = new Properties();
        final ConfigSorter configSorter = new ConfigSorter(properties);

        final ConfigSorterException configSorterException = assertThrows(ConfigSorterException.class, configSorter::sortProperties);
        assertEquals("The following configuration fields are not set: [db_users_password, db_users_max_open, db_work_password, db_work_max_open, app_node_name, db_users_url, app_jwt_ttl, db_work_username, db_users_min_idle, app_max_login_attempts, db_work_max_idle, db_users_driver, db_users_max_idle, db_work_url, db_work_driver, db_work_min_idle, db_users_username, app_rsa_ttl]", configSorterException.getMessage());
    }

    @Test
    public void sortProperties_shouldNotThrowException_whenAllKeysAreSet() throws ConfigSorterException {
        final Properties properties     = new Properties();
        final ConfigSorter configSorter = new ConfigSorter(properties);

        properties.setProperty(DB_USERS_ENUM.DB_DRIVER.key, "driver");
        properties.setProperty(DB_USERS_ENUM.DB_MAX_OPEN.key, "1000");
        properties.setProperty(DB_USERS_ENUM.DB_MAX_IDLE.key, "30s");
        properties.setProperty(DB_USERS_ENUM.DB_MIN_IDLE.key, "10s");
        properties.setProperty(DB_USERS_ENUM.DB_URL.key, "jdbc:postgres://localhost/db");
        properties.setProperty(DB_USERS_ENUM.DB_USERNAME.key, "username");
        properties.setProperty(DB_USERS_ENUM.DB_PASSWORD.key, "password");

        properties.setProperty(DB_WORK_ENUM.DB_DRIVER.key, "driver");
        properties.setProperty(DB_WORK_ENUM.DB_MAX_OPEN.key, "1000");
        properties.setProperty(DB_WORK_ENUM.DB_MAX_IDLE.key, "30s");
        properties.setProperty(DB_WORK_ENUM.DB_MIN_IDLE.key, "10s");
        properties.setProperty(DB_WORK_ENUM.DB_URL.key, "jdbc:postgres://localhost/db");
        properties.setProperty(DB_WORK_ENUM.DB_USERNAME.key, "username");
        properties.setProperty(DB_WORK_ENUM.DB_PASSWORD.key, "password");

        properties.setProperty(APP_ENUM.APP_JWT_TTL.key, "500");
        properties.setProperty(APP_ENUM.APP_MAX_LOGIN_ATTEMPTS.key, "30");
        properties.setProperty(APP_ENUM.APP_NODE_NAME.key, "nodeName");
        properties.setProperty(APP_ENUM.APP_RSA_TTL.key, "1800");


        assertDoesNotThrow(configSorter::sortProperties);
    }
}
