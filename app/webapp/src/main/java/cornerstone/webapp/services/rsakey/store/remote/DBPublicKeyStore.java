package cornerstone.webapp.services.rsakey.store.remote;

import cornerstone.webapp.services.rsakey.common.PublicKeyData;
import cornerstone.webapp.logmessages.ServiceLogMessages;
import cornerstone.webapp.datasources.WorkDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.sql.*;
import java.util.NoSuchElementException;
import java.util.UUID;

public class DBPublicKeyStore implements DBPublicKeyStoreInterface {
    private static final Logger logger = LoggerFactory.getLogger(DBPublicKeyStore.class);
    private static final String SQL_INSERT_PUBLIC_KEY          = "INSERT INTO secure.public_keys (uuid, node_name, ttl, base64_key) VALUES(?,?,?,?)";

    private static final String SQL_GET_ACTIVE_PUBLIC_KEY       = "SELECT base64_key,expire_ts FROM secure.public_keys WHERE uuid=? AND expire_ts >= NOW()";
    private static final String SQL_GET_ACTIVE_PUBLIC_KEYS      = "SELECT uuid,base64_key,expire_ts FROM secure.public_keys WHERE expire_ts >= NOW()";
    private static final String SQL_DELETE_PUBLIC_KEY           = "DELETE FROM secure.public_keys WHERE uuid=?";
    private static final String SQL_DELETE_EXPIRED_PUBLIC_KEYS  = "DELETE FROM secure.public_keys WHERE expire_ts < NOW()";

    // app conf ssl ttl
    // TODO messages
    private static final String ERROR_MSG_FAILED_TO_STORE_PUBLIC_KEY  = "Failed to store public key! UUID: '%s', message: '%s', SQL state: '%s', error code: '%s'";
    private static final String ERROR_MSG_FAILED_TO_DELETE_PUBLIC_KEY = "Failed to delete public key! UUID: '%s', message: '%s', SQL state: '%s', error code: '%s'";

    private final WorkDB workDB;

    @Inject
    public DBPublicKeyStore(final WorkDB workDB) {
        this.workDB = workDB;
        logger.info(ServiceLogMessages.MESSAGE_INSTANCE_CREATED);
    }

    @Override
    public int addPublicKey(final UUID uuid, final String node_name, final int ttl, final String base64_key ) throws DBPublicKeyStoreException {
        try (final Connection c = workDB.getConnection();
             final PreparedStatement ps = c.prepareStatement(SQL_INSERT_PUBLIC_KEY)) {

            ps.setObject(1, uuid);
            ps.setString(2, node_name);
            ps.setInt(3, ttl);
            ps.setString(4, base64_key);

            return ps.executeUpdate();

        } catch (final SQLException e) {
            final String msg = String.format(ERROR_MSG_FAILED_TO_STORE_PUBLIC_KEY, uuid.toString(), e.getMessage(), e.getSQLState(), e.getErrorCode());
            logger.error(msg);
            throw new DBPublicKeyStoreException(msg);
        }
    }

    @Override
    public int removePublicKey(final UUID uuid) throws DBPublicKeyStoreException {
        try (final Connection c = workDB.getConnection();
             final PreparedStatement ps = c.prepareStatement(SQL_DELETE_PUBLIC_KEY)) {

            ps.setObject(1, uuid);
            return ps.executeUpdate();

        } catch (final SQLException e) {
            final String msg = String.format(ERROR_MSG_FAILED_TO_DELETE_PUBLIC_KEY, uuid.toString(), e.getMessage(), e.getSQLState(), e.getErrorCode());
            logger.error(msg);
            throw new DBPublicKeyStoreException(msg);
        }
    }

    @Override
    public PublicKeyData getPublicKey(final UUID uuid) throws DBPublicKeyStoreException {
        try (final Connection c = workDB.getConnection();
             final PreparedStatement ps = c.prepareStatement(SQL_GET_ACTIVE_PUBLIC_KEY)) {

            ps.setObject(1, uuid);
            final ResultSet rs = ps.executeQuery();
            if (!rs.isBeforeFirst() ) {
                // logSystem.out.println("No data");
                throw new NoSuchElementException();

            } else {
                final Timestamp expireDate = rs.getTimestamp("expire_ts");
                final String base64key = rs.getString("base64_key");
                // log some received data
                return new PublicKeyData(uuid, base64key, expireDate);
            }

        } catch (final SQLException e) {
            final String msg = String.format(ERROR_MSG_FAILED_TO_DELETE_PUBLIC_KEY, uuid.toString(), e.getMessage(), e.getSQLState(), e.getErrorCode());
            logger.error(msg);
            throw new DBPublicKeyStoreException(msg);
        }
    }
}
