package cornerstone.webapp.datasources;

import cornerstone.webapp.common.CommonLogMessages;
import cornerstone.webapp.config.ConfigLoader;
import cornerstone.webapp.config.enums.DB_WORK_ENUM;
import jakarta.inject.Inject;
import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class WorkDB extends BasicDataSource {
    private static final Logger logger = LoggerFactory.getLogger(WorkDB.class);
    
    @Inject
    public WorkDB(final ConfigLoader configLoader) {
        super();
        final Properties p = configLoader.getWorkDbProperties();
        
        setDriverClassName                          (p.getProperty(DB_WORK_ENUM.DB_DRIVER.key));
        setUrl                                      (p.getProperty(DB_WORK_ENUM.DB_URL.key));
        setUsername                                 (p.getProperty(DB_WORK_ENUM.DB_USERNAME.key));
        setPassword                                 (p.getProperty(DB_WORK_ENUM.DB_PASSWORD.key));

        setMinIdle                  (Integer.parseInt(p.getProperty(DB_WORK_ENUM.DB_MIN_IDLE.key)));
        setMaxIdle                  (Integer.parseInt(p.getProperty(DB_WORK_ENUM.DB_MAX_IDLE.key)));
        setMaxOpenPreparedStatements(Integer.parseInt(p.getProperty(DB_WORK_ENUM.DB_MAX_OPEN.key)));

        logger.info(String.format(CommonLogMessages.MESSAGE_CONSTRUCTOR_CALLED, this.getClass().getName()));
    }
}
