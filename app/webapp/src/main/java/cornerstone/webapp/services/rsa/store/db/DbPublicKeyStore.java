package cornerstone.webapp.services.rsa.store.db;

import cornerstone.webapp.datasources.WorkDB;
import cornerstone.webapp.common.DefaultLogMessages;
import cornerstone.webapp.services.rsa.common.PublicKeyData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.sql.*;
import java.util.NoSuchElementException;
import java.util.UUID;

public class DbPublicKeyStore implements DbPublicKeyStoreInterface {
    private static final Logger logger = LoggerFactory.getLogger(DbPublicKeyStore.class);

    // SQL
    private static final String SQL_GET_ACTIVE_PUBLIC_KEY       = "SELECT base64_key,expire_ts FROM secure.public_keys WHERE uuid=? AND expire_ts >= NOW()";
    private static final String SQL_INSERT_PUBLIC_KEY           = "INSERT INTO secure.public_keys (uuid, node_name, ttl, base64_key) VALUES(?,?,?,?)";
    private static final String SQL_DELETE_PUBLIC_KEY           = "DELETE FROM secure.public_keys WHERE uuid=?";
    private static final String SQL_GET_ACTIVE_PUBLIC_KEYS      = "SELECT uuid,base64_key,expire_ts FROM secure.public_keys WHERE expire_ts >= NOW()";
    private static final String SQL_DELETE_EXPIRED_PUBLIC_KEYS  = "DELETE FROM secure.public_keys WHERE expire_ts < NOW()";

    // messages
    private static final String MESSAGE_PUBLIC_KEY_DELETED   = "... public key DELETED   (UUID: '%s')";
    private static final String MESSAGE_PUBLIC_KEY_INSERTED  = "... public key INSERTED  (UUID: '%s')";
    private static final String MESSAGE_PUBLIC_KEY_RETRIEVED = "... public key RETRIEVED (UUID: '%s')";
    // error messages
    private static final String ERROR_MESSAGE_FAILED_TO_INSERT_PUBLIC_KEY = "Failed to INSERT public key! UUID: '%s', message: '%s', SQL state: '%s', error code: '%s'";
    private static final String ERROR_MESSAGE_FAILED_TO_DELETE_PUBLIC_KEY = "Failed to DELETE public key! UUID: '%s', message: '%s', SQL state: '%s', error code: '%s'";

    private final WorkDB workDB;

    @Inject
    public DbPublicKeyStore(final WorkDB workDB) {
        this.workDB = workDB;
        logger.info(String.format(DefaultLogMessages.MESSAGE_CONSTRUCTOR_CALLED, getClass().getName()));
    }

    @Override
    public int addPublicKey(final UUID uuid, final String node_name, final int ttl, final String base64_key ) throws DbPublicKeyStoreException {
        try (final Connection c = workDB.getConnection();
             final PreparedStatement ps = c.prepareStatement(SQL_INSERT_PUBLIC_KEY)) {

            ps.setObject(1, uuid);
            ps.setString(2, node_name);
            ps.setInt(3, ttl);
            ps.setString(4, base64_key);
            logger.info(String.format(MESSAGE_PUBLIC_KEY_INSERTED, uuid));
            return ps.executeUpdate();

        } catch (final SQLException e) {
            final String msg = String.format(ERROR_MESSAGE_FAILED_TO_INSERT_PUBLIC_KEY, uuid.toString(), e.getMessage(), e.getSQLState(), e.getErrorCode());
            logger.error(msg);
            throw new DbPublicKeyStoreException(msg);
        }
    }

    @Override
    public int removePublicKey(final UUID uuid) throws DbPublicKeyStoreException {
        try (final Connection c = workDB.getConnection();
             final PreparedStatement ps = c.prepareStatement(SQL_DELETE_PUBLIC_KEY)) {

            ps.setObject(1, uuid);
            logger.info(String.format(MESSAGE_PUBLIC_KEY_DELETED, uuid));
            return ps.executeUpdate();

        } catch (final SQLException e) {
            final String msg = String.format(ERROR_MESSAGE_FAILED_TO_DELETE_PUBLIC_KEY, uuid.toString(), e.getMessage(), e.getSQLState(), e.getErrorCode());
            logger.error(msg);
            throw new DbPublicKeyStoreException(msg);
        }
    }

    @Override
    public PublicKeyData getPublicKey(final UUID uuid) throws DbPublicKeyStoreException {
        try (final Connection c = workDB.getConnection();
             final PreparedStatement ps = c.prepareStatement(SQL_GET_ACTIVE_PUBLIC_KEY)) {

            ps.setObject(1, uuid);
            final ResultSet rs = ps.executeQuery();
            if (!rs.isBeforeFirst()) {
                logger.error(String.format(MESSAGE_PUBLIC_KEY_INSERTED, uuid));
                throw new NoSuchElementException();

            } else {
                final Timestamp expireDate = rs.getTimestamp("expire_ts");
                final String base64key = rs.getString("base64_key");
                logger.info(String.format(MESSAGE_PUBLIC_KEY_RETRIEVED, uuid));

                return new PublicKeyData(uuid, base64key, expireDate);
            }

        } catch (final SQLException e) {
            final String msg = String.format(ERROR_MESSAGE_FAILED_TO_DELETE_PUBLIC_KEY, uuid.toString(), e.getMessage(), e.getSQLState(), e.getErrorCode());
            logger.error(msg);
            throw new DbPublicKeyStoreException(msg);
        }
    }
}
