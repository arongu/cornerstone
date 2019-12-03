package cornerstone.workflow.app.datasource;

import cornerstone.workflow.app.configuration.ConfigurationProvider;
import cornerstone.workflow.app.configuration.DBConfigurationField;
import org.apache.commons.dbcp2.BasicDataSource;

import javax.inject.Inject;
import java.util.Properties;

// main database connection provider (singleton -- consult with JerseyBinder.class)
public class DataDB extends BasicDataSource {
    @Inject
    public DataDB(final ConfigurationProvider configurationProvider) {
        super();
        Properties properties = configurationProvider.get_data_db_properties();
        this.setDriverClassName(properties.getProperty(DBConfigurationField.DB_DATA_DRIVER.getKey()));
        this.setUrl(properties.getProperty(DBConfigurationField.DB_DATA_URL.getKey()));
        this.setUsername(properties.getProperty(DBConfigurationField.DB_DATA_USER.getKey()));
        this.setPassword(properties.getProperty(DBConfigurationField.DB_DATA_PASSWORD.getKey()));
        this.setMinIdle(Integer.parseInt(properties.getProperty(DBConfigurationField.DB_DATA_MIN_IDLE.getKey())));
        this.setMaxIdle(Integer.parseInt(properties.getProperty(DBConfigurationField.DB_DATA_MAX_IDLE.getKey())));
        this.setMaxOpenPreparedStatements(Integer.parseInt(properties.getProperty(DBConfigurationField.DB_DATA_MAX_OPEN.getKey())));
    }
}
