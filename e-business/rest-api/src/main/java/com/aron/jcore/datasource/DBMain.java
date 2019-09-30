package com.aron.jcore.datasource;

import com.aron.jcore.config.ConfigurationProvider;
import com.aron.jcore.config.DBConfigurationField;
import org.apache.commons.dbcp2.BasicDataSource;

import javax.inject.Inject;
import java.util.Properties;

public class DBMain extends BasicDataSource {
    @Inject
    public DBMain(final ConfigurationProvider configurationProvider) {
        super();
        Properties properties = configurationProvider.getMainDBproperties();
        this.setDriverClassName(properties.getProperty(DBConfigurationField.DB_MAIN_DRIVER.getKey()));
        this.setUrl(properties.getProperty(DBConfigurationField.DB_MAIN_URL.getKey()));
        this.setUsername(properties.getProperty(DBConfigurationField.DB_MAIN_USER.getKey()));
        this.setPassword(properties.getProperty(DBConfigurationField.DB_MAIN_PASSWORD.getKey()));
        this.setMinIdle(Integer.parseInt(properties.getProperty(DBConfigurationField.DB_MAIN_MIN_IDLE.getKey())));
        this.setMaxIdle(Integer.parseInt(properties.getProperty(DBConfigurationField.DB_MAIN_MAX_IDLE.getKey())));
        this.setMaxOpenPreparedStatements(Integer.parseInt(properties.getProperty(DBConfigurationField.DB_MAIN_MAX_OPEN.getKey())));
    }
}
