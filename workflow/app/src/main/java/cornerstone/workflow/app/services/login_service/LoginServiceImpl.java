package cornerstone.workflow.app.services.login_service;

import cornerstone.workflow.app.configuration.ConfigurationProvider;
import cornerstone.workflow.app.datasource.AccountDB;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.apache.commons.codec.digest.Crypt;
import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.security.Key;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Objects;

public class LoginServiceImpl implements LoginService {
    private static final Logger logger = LoggerFactory.getLogger(LoginServiceImpl.class);
    private static final String SQL_GET_ACCOUNT_ENABLED_AND_PASSWORD = "SELECT account_enabled, password_hash FROM accounts WHERE email_address=(?)";

    private BasicDataSource dataSource;
    private Key key;

    @Inject
    public LoginServiceImpl(final AccountDB accountDB, final ConfigurationProvider configurationProvider) {
        this.dataSource = accountDB;
        loadKey(configurationProvider);
    }

    @Override
    public boolean authenticate(final String email, final String password) {
        try (final Connection conn = dataSource.getConnection()) {
            try (final PreparedStatement ps = conn.prepareStatement(SQL_GET_ACCOUNT_ENABLED_AND_PASSWORD)) {
                ps.setString(1, email.toLowerCase());

                final ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    if ( Objects.equals(rs.getString("account_enabled"), "t")) {
                        final String storedPasswordHash = rs.getString("password_hash");
                        return Objects.equals(storedPasswordHash, Crypt.crypt(password, storedPasswordHash));
                    }
                }
            }
        } catch (final SQLException e) {
            logger.error("Failed to query password for account: '{}' due to the following error: '{}'", email, e.getMessage());
        }

        return false;
    }

    @Override
    public String issueJWT(final String email) {
        return Jwts.builder().setSubject(email).signWith(key).compact();
    }

    public void loadKey(final ConfigurationProvider configurationProvider){
        final String base64key = (String) configurationProvider.getProperties().get("api_hmac_key");
        if ( null !=  base64key ) {
            this.key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(base64key));
        } else {
            logger.error("HMAC key for JWT token generation is set to null, the app will not work correctly!");
        }
    }
}

