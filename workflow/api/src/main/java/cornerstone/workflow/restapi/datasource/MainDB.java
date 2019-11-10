package cornerstone.workflow.restapi.datasource;

import cornerstone.workflow.restapi.config.ConfigurationProvider;
import cornerstone.workflow.restapi.config.DBConfigurationField;
import org.apache.commons.dbcp2.BasicDataSource;

import javax.inject.Inject;
import java.util.Properties;

// main database connection provider (singleton -- consult with JerseyBinder.class)
public class MainDB extends BasicDataSource {
    @Inject
    public MainDB(final ConfigurationProvider configurationProvider) {
        super();
        Properties properties = configurationProvider.getMainDBproperties();
        this.setDriverClassName(properties.getProperty(DBConfigurationField.DB_MAIN_DRIVER.getKey()));
        this.setUrl(properties.getProperty(DBConfigurationField.DB_MAIN_URL.getKey()));
        this.setUsername(properties.getProperty(DBConfigurationField.DB_MAIN_USER.getKey()));
        this.setPassword(properties.getProperty(DBConfigurationField.DB_MAIN_PASSWORD.getKey()));
        this.setMinIdle(Integer.parseInt(properties.getProperty(DBConfigurationField.DB_MAIN_MIN_IDLE.getKey())));
        this.setMaxIdle(Integer.parseInt(properties.getProperty(DBConfigurationField.DB_MAIN_MAX_IDLE.getKey())));
        this.setMaxOpenPreparedStatements(Integer.parseInt(properties.getProperty(DBConfigurationField.DB_MAIN_MAX_OPEN.getKey())));
    }
}
