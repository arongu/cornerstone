package cornerstone.webapp.configuration;

import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ConfigSorterTest {
    // All set -> no exception
    // Check passwords are hidden,

    @Test
    public void sortProperties_shouldThrowException_whenAllTheRequiredKeysAreMissing() throws ConfigSorterException {
        final Properties properties = new Properties();
        ConfigSorter configSorter = new ConfigSorter(properties);

        final ConfigSorterException configSorterException = assertThrows(ConfigSorterException.class, configSorter::sortProperties);
        assertEquals("The following configuration fields are not set: [db_users_password, db_users_max_open, db_work_password, db_work_max_open, app_node_name, db_users_url, app_jwt_ttl, db_work_username, db_users_min_idle, app_max_login_attempts, db_work_max_idle, db_users_driver, db_users_max_idle, db_work_url, db_work_driver, db_work_min_idle, db_users_username, app_rsa_ttl]", configSorterException.getMessage());
    }

    @Test
    public void x() {

    }
}
