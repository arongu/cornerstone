package cornerstone.workflow.webapp.datasource;

import cornerstone.workflow.webapp.configuration.ConfigurationLoader;
import cornerstone.workflow.webapp.configuration.enums.DB_WORK_ENUM;
import org.apache.commons.dbcp2.BasicDataSource;

import javax.inject.Inject;
import java.util.Properties;

// main/work database connection provider (singleton -- consult with JerseyBinder.class)
public class DataSourceWorkDB extends BasicDataSource {
    
    @Inject
    public DataSourceWorkDB(final ConfigurationLoader configurationLoader) {
        super();
        final Properties workDbProps = configurationLoader.getWorkDbProperties();
        
        setDriverClassName(
                workDbProps.getProperty(DB_WORK_ENUM.DB_DRIVER.key)
        );

        setUrl(
                workDbProps.getProperty(DB_WORK_ENUM.DB_URL.key)
        );

        setUsername(
                workDbProps.getProperty(DB_WORK_ENUM.DB_USER.key)
        );

        setPassword(
                workDbProps.getProperty(DB_WORK_ENUM.DB_PASSWORD.key)
        );

        setMinIdle(
                Integer.parseInt(
                        workDbProps.getProperty(DB_WORK_ENUM.DB_MIN_IDLE.key)
                )
        );

        setMaxIdle(
                Integer.parseInt(
                        workDbProps.getProperty(DB_WORK_ENUM.DB_MAX_IDLE.key)
                )
        );

        setMaxOpenPreparedStatements(
                Integer.parseInt(
                        workDbProps.getProperty(DB_WORK_ENUM.DB_MAX_OPEN.key)
                )
        );
    }
}
