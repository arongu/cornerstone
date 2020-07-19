package cornerstone.workflow.webapp.datasources;

import cornerstone.workflow.webapp.configuration.ConfigurationLoader;
import cornerstone.workflow.webapp.configuration.enums.DB_WORK_ENUM;
import cornerstone.workflow.webapp.logmessages.ServiceLogMessages;
import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Properties;

// main/work database connection provider (singleton JerseyBinder.class)
public class WorkDB extends BasicDataSource {
    private static final Logger logger = LoggerFactory.getLogger(WorkDB.class);
    
    @Inject
    public WorkDB(final ConfigurationLoader configurationLoader) {
        super();
        final Properties p = configurationLoader.getWorkDbProperties();
        
        setDriverClassName                          (p.getProperty(DB_WORK_ENUM.DB_DRIVER.key));
        setUrl                                      (p.getProperty(DB_WORK_ENUM.DB_URL.key));
        setUsername                                 (p.getProperty(DB_WORK_ENUM.DB_USERNAME.key));
        setPassword                                 (p.getProperty(DB_WORK_ENUM.DB_PASSWORD.key));

        setMinIdle                  (Integer.parseInt(p.getProperty(DB_WORK_ENUM.DB_MIN_IDLE.key)));
        setMaxIdle                  (Integer.parseInt(p.getProperty(DB_WORK_ENUM.DB_MAX_IDLE.key)));
        setMaxOpenPreparedStatements(Integer.parseInt(p.getProperty(DB_WORK_ENUM.DB_MAX_OPEN.key)));

        logger.info(String.format(ServiceLogMessages.MESSAGE_INSTANCE_CREATED, this.getClass().getName()));
    }
}
