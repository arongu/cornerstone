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
    private static final String SQL_GET_ACCOUNT_ENABLED_AND_PASSWORD = "SELECT account_enabled, password_hash FROM accounts WHERE email_address=(?)";

    private static final Logger logger = LoggerFactory.getLogger(LoginServiceImpl.class);

    private final BasicDataSource dataSource;
    private final Key key;

    @Inject
    public LoginServiceImpl(final AccountDB AccountDB, final ConfigurationProvider configurationProvider) {
        this.dataSource = AccountDB;
        String base64key = (String) configurationProvider.getProperties().get("api_hmac_key");
        byte[] ba = Base64.getDecoder().decode(base64key);
        key = Keys.hmacShaKeyFor(ba);
    }

    @Override
    public boolean authenticate(final String email, final String password) {
        try (Connection c = dataSource.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement(SQL_GET_ACCOUNT_ENABLED_AND_PASSWORD)) {
                ps.setString(1, email.toLowerCase());

                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    String enabled = rs.getString("account_enabled");
                    if ( enabled != null && Integer.parseInt(enabled) == 1) {
                        String storedPasswordHash = rs.getString("password_hash");
                        return Objects.equals(
                                storedPasswordHash,
                                Crypt.crypt(password, storedPasswordHash)
                        );
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
}
