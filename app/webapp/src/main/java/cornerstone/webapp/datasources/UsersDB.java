package cornerstone.webapp.datasources;

import cornerstone.webapp.common.DefaultLogMessages;
import cornerstone.webapp.configuration.ConfigurationLoader;
import cornerstone.webapp.configuration.enums.DB_USERS_ENUM;
import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Properties;

public class UsersDB extends BasicDataSource {
    private static final Logger logger = LoggerFactory.getLogger(UsersDB.class);

    @Inject
    public UsersDB(final ConfigurationLoader configurationLoader){
        super();
        final Properties p = configurationLoader.getUsersDbProperties();

        setDriverClassName                           (p.getProperty(DB_USERS_ENUM.DB_DRIVER.key));
        setUrl                                       (p.getProperty(DB_USERS_ENUM.DB_URL.key));
        setUsername                                  (p.getProperty(DB_USERS_ENUM.DB_USERNAME.key));
        setPassword                                  (p.getProperty(DB_USERS_ENUM.DB_PASSWORD.key));

        setMinIdle                  (Integer.parseInt(p.getProperty(DB_USERS_ENUM.DB_MIN_IDLE.key)));
        setMaxIdle                  (Integer.parseInt(p.getProperty(DB_USERS_ENUM.DB_MAX_IDLE.key)));
        setMaxOpenPreparedStatements(Integer.parseInt(p.getProperty(DB_USERS_ENUM.DB_MAX_OPEN.key)));

        logger.info(String.format(DefaultLogMessages.MESSAGE_CONSTRUCTOR_CALLED, this.getClass().getName()));
    }
}
