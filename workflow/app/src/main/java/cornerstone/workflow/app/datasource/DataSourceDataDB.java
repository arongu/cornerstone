package cornerstone.workflow.app.datasource;

import cornerstone.workflow.app.configuration.ConfigurationProvider;
import cornerstone.workflow.app.configuration.enums.ConfigFieldsDbData;
import org.apache.commons.dbcp2.BasicDataSource;

import javax.inject.Inject;
import java.util.Properties;

// main database connection provider (singleton -- consult with JerseyBinder.class)
public class DataSourceDataDB extends BasicDataSource {
    
    @Inject
    public DataSourceDataDB(final ConfigurationProvider configurationProvider) {
        super();
        final Properties props = configurationProvider.get_data_db_properties();
        
        setDriverClassName(
                props.getProperty(
                        ConfigFieldsDbData.DB_DATA_DRIVER.key
                )
        );

        setUrl(
                props.getProperty(
                        ConfigFieldsDbData.DB_DATA_URL.key
                )
        );

        setUsername(
                props.getProperty(
                        ConfigFieldsDbData.DB_DATA_USER.key
                )
        );

        setPassword(
                props.getProperty(
                        ConfigFieldsDbData.DB_DATA_PASSWORD.key
                )
        );

        setMinIdle(
                Integer.parseInt(
                        props.getProperty(
                                ConfigFieldsDbData.DB_DATA_MIN_IDLE.key
                        )
                )
        );

        setMaxIdle(
                Integer.parseInt(
                        props.getProperty(
                                ConfigFieldsDbData.DB_DATA_MAX_IDLE.key
                        )
                )
        );

        setMaxOpenPreparedStatements(
                Integer.parseInt(
                        props.getProperty(
                                ConfigFieldsDbData.DB_DATA_MAX_OPEN.key
                        )
                )
        );
    }
}
