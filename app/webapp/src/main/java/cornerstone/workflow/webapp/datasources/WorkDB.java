package cornerstone.workflow.webapp.datasources;

import cornerstone.workflow.webapp.configuration.ConfigurationLoader;
import cornerstone.workflow.webapp.configuration.enums.DB_WORK_ENUM;
import org.apache.commons.dbcp2.BasicDataSource;

import javax.inject.Inject;
import java.util.Properties;

// main/work database connection provider (singleton -- consult with JerseyBinder.class)
public class WorkDB extends BasicDataSource {
    
    @Inject
    public WorkDB(final ConfigurationLoader configurationLoader) {
        super();
        final Properties workDbProps = configurationLoader.getWorkDbProperties();
        
        setDriverClassName(
                workDbProps.getProperty(DB_WORK_ENUM.DB_DRIVER.key)
        );

        setUrl(
                workDbProps.getProperty(DB_WORK_ENUM.DB_URL.key)
        );

        setUsername(
                workDbProps.getProperty(DB_WORK_ENUM.DB_USERNAME.key)
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
