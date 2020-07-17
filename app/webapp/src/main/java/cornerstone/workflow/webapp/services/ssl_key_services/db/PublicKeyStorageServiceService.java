package cornerstone.workflow.webapp.services.ssl_key_services.db;

import cornerstone.workflow.webapp.datasource.DataSourceWorkDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

public class PublicKeyStorageServiceService implements PublicKeyStorageServiceInterface {
    private static final Logger logger = LoggerFactory.getLogger(PublicKeyStorageServiceService.class);
    private static final String SQL_INSERT_PUBLIC_KEY = "INSERT INTO secure.public_keys (uuid, node_name, ttl, base64_key) VALUES(?,?,?,?)";
    private static final String SQL_DELETE_PUBLIC_KEY = "DELETE FROM secure.public_keys WHERE uuid=?";

    private static final String ERROR_MSG_FAILED_TO_STORE_PUBLIC_KEY  = "Failed to store public key! UUID: '%s', message: '%s', SQL state: '%s', error code: '%s'";
    private static final String ERROR_MSG_FAILED_TO_DELETE_PUBLIC_KEY = "Failed to delete public key! UUID: '%s', message: '%s', SQL state: '%s', error code: '%s'";

    private final DataSourceWorkDB dataSource;

    @Inject
    public PublicKeyStorageServiceService(final DataSourceWorkDB dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public int savePublicKeyToDB(final UUID uuid, final String nodeName, final int ttl, final String base64key ) throws PublicKeyStorageServiceException {
        try (final Connection c = dataSource.getConnection();
             final PreparedStatement ps = c.prepareStatement(SQL_INSERT_PUBLIC_KEY)) {

            ps.setObject(1, uuid);
            ps.setString(2, nodeName);
            ps.setInt(3, ttl);
            ps.setString(4, base64key);

            return ps.executeUpdate();

        } catch (final SQLException e) {
            final String msg = String.format(
                    ERROR_MSG_FAILED_TO_STORE_PUBLIC_KEY, uuid.toString(), e.getMessage(), e.getSQLState(), e.getErrorCode()
            );

            logger.error(msg);
            throw new PublicKeyStorageServiceException(msg);
        }
    }

    @Override
    public int deletePublicKeyFromDB(final UUID uuid) throws PublicKeyStorageServiceException {
        try (final Connection c = dataSource.getConnection();
             final PreparedStatement ps = c.prepareStatement(SQL_DELETE_PUBLIC_KEY)) {

            ps.setObject(1, uuid);
            return ps.executeUpdate();

        } catch (final SQLException e) {
            final String msg = String.format(
                    ERROR_MSG_FAILED_TO_DELETE_PUBLIC_KEY, uuid.toString(), e.getMessage(), e.getSQLState(), e.getErrorCode()
            );

            logger.error(msg);
            throw new PublicKeyStorageServiceException(msg);
        }
    }
}
