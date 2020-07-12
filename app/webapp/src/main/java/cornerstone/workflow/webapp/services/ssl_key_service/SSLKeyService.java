package cornerstone.workflow.webapp.services.ssl_key_service;

import cornerstone.workflow.webapp.datasource.DataSourceWorkDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

public class SSLKeyService implements SSLKeyServiceInterface {
    private static final Logger logger = LoggerFactory.getLogger(SSLKeyService.class);
    private static final String SQL_INSERT_PUBLIC_KEY = "INSERT INTO secure.pubkeys (uuid, created_by, base64_public_key) VALUES(?,?,?)";
    private static final String ERROR_MSG_FAILED_TO_STORE_PUBLIC_KEY = "Failed to store public key! UUID: '%s', message: '%s', SQL state: '%s', error code: '%s'";

    private final DataSourceWorkDB dataSource;

    @Inject
    public SSLKeyService(final DataSourceWorkDB dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public int savePublicKeyToDB(final String base64pubkey, final String created_by, final UUID uuid) throws SSLKeyServiceException {
        try (final Connection c = dataSource.getConnection();
             final PreparedStatement ps = c.prepareStatement(SQL_INSERT_PUBLIC_KEY)) {

            ps.setObject(1, uuid);
            ps.setString(2, created_by);
            ps.setString(3, base64pubkey);

            return ps.executeUpdate();

        } catch (final SQLException e) {
            final String msg = String.format(
                    ERROR_MSG_FAILED_TO_STORE_PUBLIC_KEY, uuid.toString(), e.getMessage(), e.getSQLState(), e.getErrorCode()
            );

            logger.error(msg);
            throw new SSLKeyServiceException(msg);
        }
    }
}
