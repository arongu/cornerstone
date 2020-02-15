package cornerstone.workflow.app.datasource;

import cornerstone.workflow.app.configuration.ConfigurationProvider;
import cornerstone.workflow.app.configuration.enums.ConfigFieldsDbAccount;
import org.apache.commons.dbcp2.BasicDataSource;

import javax.inject.Inject;
import java.util.Properties;

// account database connection provider (singleton -- consult with JerseyBinder.class)
public class AccountDB extends BasicDataSource {

    @Inject
    public AccountDB(final ConfigurationProvider configurationProvider){
        super();
        final Properties props = configurationProvider.get_account_db_properties();

        setDriverClassName(
                props.getProperty(
                        ConfigFieldsDbAccount.DB_ACCOUNT_DRIVER.key
                )
        );

        setUrl(
                props.getProperty(
                        ConfigFieldsDbAccount.DB_ACCOUNT_URL.key
                )
        );

        setUsername(
                props.getProperty(
                        ConfigFieldsDbAccount.DB_ACCOUNT_USER.key
                )
        );

        setPassword(
                props.getProperty(
                        ConfigFieldsDbAccount.DB_ACCOUNT_PASSWORD.key
                )
        );

        setMinIdle(
                Integer.parseInt(
                        props.getProperty(
                                ConfigFieldsDbAccount.DB_ACCOUNT_MIN_IDLE.key
                        )
                )
        );

        setMaxIdle(
                Integer.parseInt(
                        props.getProperty(
                                ConfigFieldsDbAccount.DB_ACCOUNT_MAX_IDLE.key
                        )
                )
        );

        setMaxOpenPreparedStatements(
                Integer.parseInt(
                        props.getProperty(
                                ConfigFieldsDbAccount.DB_ACCOUNT_MAX_OPEN.key
                        )
                )
        );
    }
}
