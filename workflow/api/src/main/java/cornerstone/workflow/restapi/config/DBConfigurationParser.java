package cornerstone.workflow.restapi.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class DBConfigurationParser {
    private static final Logger logger = LoggerFactory.getLogger(DBConfigurationParser.class);
    private Properties mainDB, adminDB;

    public DBConfigurationParser(final Properties properties) {
       processProperties(properties);
    }

    public void processProperties(final Properties properties){
        this.mainDB = new Properties();
        this.adminDB = new Properties();

        for ( DBConfigurationField field : DBConfigurationField.values()){
            String key = field.getKey();
            if ( key.startsWith(DBConfigurationField.mainPrefix) || key.startsWith(DBConfigurationField.mgmtPrefix)) {
                String value = properties.getProperty(key);

                if ( value != null && ! value.isEmpty()) {
                    if ( key.startsWith(DBConfigurationField.mainPrefix)) {
                        this.mainDB.setProperty(key, value);
                        if ( !key.contains("password")){
                            logger.info("... main DB '{}' = '{}'", key, value);
                        } else {
                            logger.info("... main DB '{}' = '*****'", key);
                        }
                    }
                    else if ( key.startsWith(DBConfigurationField.mgmtPrefix)) {
                        this.adminDB.setProperty(key, value);
                        if ( !key.contains("password")){
                            logger.info("... admin DB '{}' = '{}'", key, value);
                        } else {
                            logger.info("... admin DB '{}' = '*****'", key);
                        }
                    }
                }
                else {
                    logger.error("... !!! '{}' is not set !!!", key);
                }
            }
            else {
                logger.info("... Ignoring '{}' .", key);
            }
        }
    }

    public Properties getMainDB() {
        return new Properties(mainDB);
    }

    public Properties getAdminDB() {
        return new Properties(adminDB);
    }
}
