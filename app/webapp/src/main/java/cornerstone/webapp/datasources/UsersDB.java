package cornerstone.webapp.datasources;

import cornerstone.webapp.logmsg.CommonLogMessages;
import cornerstone.webapp.configuration.ConfigLoader;
import cornerstone.webapp.configuration.enums.DB_USERS_ENUM;
import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Properties;

/**
 * Extended class of BasicDataSource.
 * This database is responsible for storing user management related information.
 */
public class UsersDB extends BasicDataSource {
    private static final Logger logger = LoggerFactory.getLogger(UsersDB.class);

    @Inject
    public UsersDB(final ConfigLoader configLoader){
        super();
        final Properties p = configLoader.getUsersDbProperties();

        setDriverClassName                           (p.getProperty(DB_USERS_ENUM.DB_DRIVER.key));
        setUrl                                       (p.getProperty(DB_USERS_ENUM.DB_URL.key));
        setUsername                                  (p.getProperty(DB_USERS_ENUM.DB_USERNAME.key));
        setPassword                                  (p.getProperty(DB_USERS_ENUM.DB_PASSWORD.key));

        setMinIdle                  (Integer.parseInt(p.getProperty(DB_USERS_ENUM.DB_MIN_IDLE.key)));
        setMaxIdle                  (Integer.parseInt(p.getProperty(DB_USERS_ENUM.DB_MAX_IDLE.key)));
        setMaxOpenPreparedStatements(Integer.parseInt(p.getProperty(DB_USERS_ENUM.DB_MAX_OPEN.key)));

        logger.info(String.format(CommonLogMessages.MESSAGE_CONSTRUCTOR_CALLED, this.getClass().getName()));
    }
}
