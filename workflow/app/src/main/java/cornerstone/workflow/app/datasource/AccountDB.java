package cornerstone.workflow.app.datasource;

import cornerstone.workflow.app.configuration.ConfigurationProvider;
import cornerstone.workflow.app.configuration.ConfigurationField;
import org.apache.commons.dbcp2.BasicDataSource;

import javax.inject.Inject;
import java.util.Properties;

// account database connection provider (singleton -- consult with JerseyBinder.class)
public class AccountDB extends BasicDataSource {
    @Inject
    public AccountDB(final ConfigurationProvider configurationProvider){
        super();
        Properties properties = configurationProvider.get_account_db_properties();
        this.setDriverClassName(properties.getProperty(ConfigurationField.DB_ACCOUNT_DRIVER.getKey()));
        this.setUrl(properties.getProperty(ConfigurationField.DB_ACCOUNT_URL.getKey()));
        this.setUsername(properties.getProperty(ConfigurationField.DB_ACCOUNT_USER.getKey()));
        this.setPassword(properties.getProperty(ConfigurationField.DB_ACCOUNT_PASSWORD.getKey()));
        this.setMinIdle(Integer.parseInt(properties.getProperty(ConfigurationField.DB_ACCOUNT_MIN_IDLE.getKey())));
        this.setMaxIdle(Integer.parseInt(properties.getProperty(ConfigurationField.DB_ACCOUNT_MAX_IDLE.getKey())));
        this.setMaxOpenPreparedStatements(Integer.parseInt(properties.getProperty(ConfigurationField.DB_ACCOUNT_MAX_OPEN.getKey())));
    }
}
