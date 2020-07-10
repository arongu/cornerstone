package cornerstone.workflow.webapp.datasource;

import cornerstone.workflow.webapp.configuration.ConfigurationLoader;
import cornerstone.workflow.webapp.configuration.enums.DB_USERS_ENUM;
import org.apache.commons.dbcp2.BasicDataSource;

import javax.inject.Inject;
import java.util.Properties;

// account database connection provider (singleton -- consult with JerseyBinder.class)
public class DataSourceUsersDB extends BasicDataSource {

    @Inject
    public DataSourceUsersDB(final ConfigurationLoader configurationLoader){
        super();
        final Properties props = configurationLoader.getUsersDbProperties();

        setDriverClassName(
                props.getProperty(DB_USERS_ENUM.DB_DRIVER.key)
        );

        setUrl(
                props.getProperty(DB_USERS_ENUM.DB_URL.key)
        );

        setUsername(
                props.getProperty(DB_USERS_ENUM.DB_USER.key)
        );

        setPassword(
                props.getProperty(DB_USERS_ENUM.DB_PASSWORD.key)
        );

        setMinIdle(
                Integer.parseInt(
                        props.getProperty(DB_USERS_ENUM.DB_MIN_IDLE.key)
                )
        );

        setMaxIdle(
                Integer.parseInt(
                        props.getProperty(DB_USERS_ENUM.DB_MAX_IDLE.key)
                )
        );

        setMaxOpenPreparedStatements(
                Integer.parseInt(
                        props.getProperty(DB_USERS_ENUM.DB_MAX_OPEN.key)
                )
        );
    }
}
