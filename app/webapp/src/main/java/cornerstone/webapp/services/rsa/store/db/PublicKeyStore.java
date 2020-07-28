package cornerstone.webapp.services.rsa.store.db;

import cornerstone.webapp.common.DefaultLogMessages;
import cornerstone.webapp.datasources.WorkDB;
import cornerstone.webapp.services.rsa.common.PublicKeyData;
import cornerstone.webapp.services.rsa.store.LoggingMessageFormats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.sql.*;
import java.util.*;

public class PublicKeyStore implements PublicKeyStoreInterface {
    private static final Logger logger = LoggerFactory.getLogger(PublicKeyStore.class);

    // SQL queries
    private static final String SQL_SELECT_PUBLIC_KEY                   = "SELECT node_name,ttl,creation_ts,expire_ts,base64_key FROM secure.public_keys WHERE uuid=?";
    private static final String SQL_INSERT_PUBLIC_KEY                   = "INSERT INTO secure.public_keys (uuid, node_name, ttl, base64_key) VALUES(?,?,?,?)";
    private static final String SQL_DELETE_PUBLIC_KEY                   = "DELETE FROM secure.public_keys WHERE uuid=?";
    private static final String SQL_SELECT_NON_EXPIRED_PUBLIC_KEYS      = "SELECT uuid,node_name,ttl,creation_ts,expire_ts,base64_key FROM secure.public_keys WHERE expire_ts > NOW()";
    private static final String SQL_SELECT_NON_EXPIRED_PUBLIC_KEY_UUIDS = "SELECT uuid FROM secure.public_keys WHERE expire_ts > NOW()";
    private static final String SQL_SELECT_EXPIRED_PUBLIC_KEY_UUIDS     = "SELECT uuid FROM secure.public_keys WHERE expire_ts < NOW()";
    private static final String SQL_DELETE_EXPIRED_PUBLIC_KEYS          = "DELETE FROM secure.public_keys WHERE expire_ts < NOW()";

    // sql-error log messages
    private static final String ERROR_MESSAGE_FAILED_TO_SELECT_PUBLIC_KEY               = "Failed to SELECT public key! UUID: '%s', message: '%s', SQL state: '%s', error code: '%s'";
    private static final String ERROR_MESSAGE_FAILED_TO_SELECT_ACTIVE_PUBLIC_KEYS       = "Failed to SELECT active public keys! message: '%s', SQL state: '%s', error code: '%s'";
    private static final String ERROR_MESSAGE_FAILED_TO_SELECT_EXPIRED_PUBLIC_KEY_UUIDS = "Failed to SELECT expired public keys! message: '%s', SQL state: '%s', error code: '%s'";
    private static final String ERROR_MESSAGE_FAILED_TO_INSERT_PUBLIC_KEY               = "Failed to INSERT public key! UUID: '%s', message: '%s', SQL state: '%s', error code: '%s'";
    private static final String ERROR_MESSAGE_FAILED_TO_DELETE_PUBLIC_KEY               = "Failed to DELETE public key! UUID: '%s', message: '%s', SQL state: '%s', error code: '%s'";
    private static final String ERROR_MESSAGE_FAILED_TO_DELETE_EXPIRED_PUBLIC_KEYS      = "Failed to DELETE expired public keys! message: '%s', SQL state: '%s', error code: '%s'";

    private final WorkDB workDB;

    @Inject
    public PublicKeyStore(final WorkDB workDB) {
        this.workDB = workDB;
        logger.info(String.format(DefaultLogMessages.MESSAGE_CONSTRUCTOR_CALLED, getClass().getName()));
    }

    @Override
    public int addKey(final UUID uuid, final String node_name, final int ttl, final String base64_key ) throws PublicKeyStoreException {
        try (final Connection c = workDB.getConnection(); final PreparedStatement ps = c.prepareStatement(SQL_INSERT_PUBLIC_KEY)) {
            ps.setObject(1, uuid);
            ps.setString(2, node_name);
            ps.setInt(3, ttl);
            ps.setString(4, base64_key);

            logger.info(String.format(LoggingMessageFormats.MESSAGE_FORMAT_SPACES_FIELD1_DATA, "  ", "ADDED PUBLIC KEY WITH UUID (DB)", uuid));
            return ps.executeUpdate();

        } catch (final SQLException e) {
            logger.error(String.format(ERROR_MESSAGE_FAILED_TO_INSERT_PUBLIC_KEY, uuid.toString(), e.getMessage(), e.getSQLState(), e.getErrorCode()));
            throw new PublicKeyStoreException();
        }
    }

    @Override
    public int deleteKey(final UUID uuid) throws PublicKeyStoreException {
        try (final Connection c = workDB.getConnection(); final PreparedStatement ps = c.prepareStatement(SQL_DELETE_PUBLIC_KEY)) {
            ps.setObject(1, uuid);
            logger.info(String.format(LoggingMessageFormats.MESSAGE_FORMAT_SPACES_FIELD1_DATA, "  ", "DELETED PUBLIC KEY WITH UUID (DB)", uuid));
            return ps.executeUpdate();

        } catch (final SQLException e) {
            logger.error(String.format(ERROR_MESSAGE_FAILED_TO_DELETE_PUBLIC_KEY, uuid.toString(), e.getMessage(), e.getSQLState(), e.getErrorCode()));
            throw new PublicKeyStoreException();
        }
    }

    @Override
    public PublicKeyData getKey(final UUID uuid) throws PublicKeyStoreException {
        try (final Connection c = workDB.getConnection(); final PreparedStatement ps = c.prepareStatement(SQL_SELECT_PUBLIC_KEY)) {
            ps.setObject(1, uuid);
            final ResultSet rs = ps.executeQuery();

            if (!rs.isBeforeFirst()) {
                logger.info(String.format(LoggingMessageFormats.MESSAGE_FORMAT_SPACES_FIELD1_DATA, "  ", "NO SUCH PUBLIC KEY WITH UUID (DB)", uuid));
                throw new NoSuchElementException();

            } else {
                final PublicKeyData keyData = new PublicKeyData(
                        uuid,
                        rs.getString("node_name"),
                        rs.getInt("ttl"),
                        rs.getTimestamp("creation_ts"),
                        rs.getTimestamp("expire_ts"),
                        rs.getString("base64_key")
                );

                logger.info(String.format(LoggingMessageFormats.MESSAGE_FORMAT_SPACES_FIELD1_DATA, "  ", "RETRIEVED PUBLIC KEY (DB)", keyData));
                return keyData;
            }

        } catch (final SQLException e) {
            logger.error(String.format(ERROR_MESSAGE_FAILED_TO_SELECT_PUBLIC_KEY, uuid.toString(), e.getMessage(), e.getSQLState(), e.getErrorCode()));
            throw new PublicKeyStoreException();
        }
    }

    @Override
    public List<PublicKeyData> getLiveKeys() throws PublicKeyStoreException {
        try (final Connection c = workDB.getConnection(); final PreparedStatement ps = c.prepareStatement(SQL_SELECT_NON_EXPIRED_PUBLIC_KEYS)) {
            final ResultSet rs = ps.executeQuery();
            final List<PublicKeyData> keys = new LinkedList<>();

            while (rs.next()){
                final UUID uuid = (UUID) rs.getObject("uuid");
                final int ttl = rs.getInt("ttl");
                final String node_name = rs.getString("node_name");
                final Timestamp creation_ts = rs.getTimestamp("creation_ts");
                final Timestamp expire_ts = rs.getTimestamp("expire_ts");
                final String base64_key = rs.getString("base64_key");
                final PublicKeyData keyData = new PublicKeyData(uuid, node_name, ttl, creation_ts, expire_ts, base64_key);
                keys.add(keyData);
            }
            logger.info(String.format(LoggingMessageFormats.MESSAGE_FORMAT_SPACES_FIELD1_DATA, "  ", "NUMBER OF RETRIEVED PUBLIC KEYS (DB)", keys.size()));
            logger.info(String.format(LoggingMessageFormats.MESSAGE_FORMAT_SPACES_FIELD1_DATA, "  ", "RETRIEVED PUBLIC KEYS (DB)", keys));
            return keys;

        } catch (final SQLException e) {
            logger.error(String.format(ERROR_MESSAGE_FAILED_TO_SELECT_ACTIVE_PUBLIC_KEYS, e.getMessage(), e.getSQLState(), e.getErrorCode()));
            throw new PublicKeyStoreException();
        }
    }

    @Override
    public List<UUID> getLiveKeyUUIDs() throws PublicKeyStoreException {
        try (final Connection c = workDB.getConnection(); final PreparedStatement ps = c.prepareStatement(SQL_SELECT_NON_EXPIRED_PUBLIC_KEY_UUIDS)) {
            final ResultSet rs = ps.executeQuery();
            final List<UUID> uuids = new LinkedList<>();

            while (rs.next()){
                uuids.add((UUID) rs.getObject("uuid"));
            }

            logger.info(String.format(LoggingMessageFormats.MESSAGE_FORMAT_SPACES_FIELD1_DATA, "  ", "NUMBER OF RETRIEVED PUBLIC KEY UUIDS (DB)", uuids.size()));
            logger.info(String.format(LoggingMessageFormats.MESSAGE_FORMAT_SPACES_FIELD1_DATA, "  ", "RETRIEVED PUBLIC KEY UUIDS (DB)", uuids));
            return uuids;

        } catch (final SQLException e) {
            logger.error(String.format(ERROR_MESSAGE_FAILED_TO_SELECT_EXPIRED_PUBLIC_KEY_UUIDS, e.getMessage(), e.getSQLState(), e.getErrorCode()));
            throw new PublicKeyStoreException();
        }
    }

    @Override
    public List<UUID> getExpiredKeyUUIDs() throws PublicKeyStoreException, NoSuchElementException {
        try (final Connection c = workDB.getConnection(); final PreparedStatement ps = c.prepareStatement(SQL_SELECT_EXPIRED_PUBLIC_KEY_UUIDS)) {
            final ResultSet rs = ps.executeQuery();
            final List<UUID> expired_uuids = new ArrayList<>();

            while (rs.next()){
                expired_uuids.add((UUID) rs.getObject("uuid"));
            }

            logger.info(String.format(LoggingMessageFormats.MESSAGE_FORMAT_SPACES_FIELD1_DATA, "  ", "NUMBER OF EXPIRED PUBLIC KEY UUIDS RETRIEVED (DB)", expired_uuids.size()));
            logger.info(String.format(LoggingMessageFormats.MESSAGE_FORMAT_SPACES_FIELD1_DATA, "  ", "RETRIEVED EXPIRED PUBLIC KEY UUIDS (DB)", expired_uuids));
            return expired_uuids;

        } catch (final SQLException e) {
            logger.error(String.format(ERROR_MESSAGE_FAILED_TO_SELECT_EXPIRED_PUBLIC_KEY_UUIDS, e.getMessage(), e.getSQLState(), e.getErrorCode()));
            throw new PublicKeyStoreException();
        }
    }

    @Override
    public int deleteExpiredKeys() throws PublicKeyStoreException {
        try (final Connection c = workDB.getConnection(); final PreparedStatement ps = c.prepareStatement(SQL_DELETE_EXPIRED_PUBLIC_KEYS)) {
            final int deletes = ps.executeUpdate();
            logger.info(String.format(LoggingMessageFormats.MESSAGE_FORMAT_SPACES_FIELD1_DATA, "  ", "EXPIRED PUBLIC KEYS DELETED (DB)", deletes));
            return deletes;

        } catch (final SQLException e) {
            logger.error(String.format(ERROR_MESSAGE_FAILED_TO_DELETE_EXPIRED_PUBLIC_KEYS, e.getMessage(), e.getSQLState(), e.getErrorCode()));
            throw new PublicKeyStoreException();
        }
    }
}
