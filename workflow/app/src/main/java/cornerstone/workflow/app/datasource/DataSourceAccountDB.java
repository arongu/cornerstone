package cornerstone.workflow.app.datasource;

import cornerstone.workflow.app.configuration.ConfigReader;
import cornerstone.workflow.app.configuration.enums.AccountDbConnectionFields;
import org.apache.commons.dbcp2.BasicDataSource;

import javax.inject.Inject;
import java.util.Properties;

// account database connection provider (singleton -- consult with JerseyBinder.class)
public class DataSourceAccountDB extends BasicDataSource {

    @Inject
    public DataSourceAccountDB(final ConfigReader configReader){
        super();
        final Properties props = configReader.getAccountDbProperties();

        setDriverClassName(
                props.getProperty(
                        AccountDbConnectionFields.DB_ACCOUNT_DRIVER.key
                )
        );

        setUrl(
                props.getProperty(
                        AccountDbConnectionFields.DB_ACCOUNT_URL.key
                )
        );

        setUsername(
                props.getProperty(
                        AccountDbConnectionFields.DB_ACCOUNT_USER.key
                )
        );

        setPassword(
                props.getProperty(
                        AccountDbConnectionFields.DB_ACCOUNT_PASSWORD.key
                )
        );

        setMinIdle(
                Integer.parseInt(
                        props.getProperty(
                                AccountDbConnectionFields.DB_ACCOUNT_MIN_IDLE.key
                        )
                )
        );

        setMaxIdle(
                Integer.parseInt(
                        props.getProperty(
                                AccountDbConnectionFields.DB_ACCOUNT_MAX_IDLE.key
                        )
                )
        );

        setMaxOpenPreparedStatements(
                Integer.parseInt(
                        props.getProperty(
                                AccountDbConnectionFields.DB_ACCOUNT_MAX_OPEN.key
                        )
                )
        );
    }
}
