package cornerstone.workflow.app.services.authentication_service;

import cornerstone.workflow.app.datasource.AccountDB;
import org.apache.commons.codec.digest.Crypt;
import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public class AuthenticationServiceImpl implements AuthenticationService {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationServiceImpl.class);
    private static final String SQL_GET_ACCOUNT_ENABLED_AND_PASSWORD = "SELECT account_available, password_hash FROM info.accounts WHERE email_address=(?)";

    private BasicDataSource dataSource;

    @Inject
    public AuthenticationServiceImpl(final AccountDB accountDB) {
        this.dataSource = accountDB;
    }

    @Override
    public boolean authenticate(final String emailAddress, final String password) {
        try (final Connection conn = dataSource.getConnection()) {
            try (final PreparedStatement ps = conn.prepareStatement(SQL_GET_ACCOUNT_ENABLED_AND_PASSWORD)) {
                ps.setString(1, emailAddress.toLowerCase());

                final ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    if ( Objects.equals(rs.getBoolean("account_available"), true)) {
                        final String storedPasswordHash = rs.getString("password_hash");
                        return Objects.equals(storedPasswordHash, Crypt.crypt(password, storedPasswordHash));
                    }
                }
            }
        } catch (final SQLException e) {
            logger.error("Failed to query password for account: '{}' due to the following error: '{}'", emailAddress, e.getMessage());
        }

        return false;
    }
}
