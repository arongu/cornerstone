package cornerstone.workflow.app.datasource;

import cornerstone.workflow.app.configuration.ConfigurationProvider;
import cornerstone.workflow.app.configuration.DBConfigurationField;
import org.apache.commons.dbcp2.BasicDataSource;

import javax.inject.Inject;
import java.util.Properties;

// user database connection provider (singleton -- consult with JerseyBinder.class)
public class UserDB extends BasicDataSource {
    @Inject
    public UserDB(final ConfigurationProvider configurationProvider){
        super();
        Properties properties = configurationProvider.getAdminDBproperties();
        this.setDriverClassName(properties.getProperty(DBConfigurationField.DB_ADMIN_DRIVER.getKey()));
        this.setUrl(properties.getProperty(DBConfigurationField.DB_ADMIN_URL.getKey()));
        this.setUsername(properties.getProperty(DBConfigurationField.DB_ADMIN_USER.getKey()));
        this.setPassword(properties.getProperty(DBConfigurationField.DB_ADMIN_PASSWORD.getKey()));
        this.setMinIdle(Integer.parseInt(properties.getProperty(DBConfigurationField.DB_ADMIN_MIN_IDLE.getKey())));
        this.setMaxIdle(Integer.parseInt(properties.getProperty(DBConfigurationField.DB_ADMIN_MAX_IDLE.getKey())));
        this.setMaxOpenPreparedStatements(Integer.parseInt(properties.getProperty(DBConfigurationField.DB_ADMIN_MAX_OPEN.getKey())));
    }
}
