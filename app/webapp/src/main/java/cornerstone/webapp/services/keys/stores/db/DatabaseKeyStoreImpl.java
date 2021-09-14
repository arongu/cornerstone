package cornerstone.webapp.services.keys.stores.db;

import cornerstone.webapp.datasources.WorkDB;
import cornerstone.webapp.logmsg.CommonLogMessages;
import cornerstone.webapp.services.keys.common.PublicKeyData;
import cornerstone.webapp.services.keys.stores.logging.KeyRelatedMessageElements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.sql.*;
import java.util.*;

/**
 * Only manages public keys in the database, does not store any keys locally, that purpose is strictly for the LocalKeyStore.
 */
public class DatabaseKeyStoreImpl implements DatabaseKeyStore {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseKeyStoreImpl.class);

    // SQL queries
    // NOTE: There are SQL triggers in the DB to calculate the expire date!
    // Insert moment NOW() + TTL(RSA key TTL + JWT TTL)
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
    public DatabaseKeyStoreImpl(final WorkDB workDB) {
        this.workDB = workDB;
        logger.info(String.format(CommonLogMessages.MESSAGE_CONSTRUCTOR_CALLED, getClass().getName()));
    }

    /**
     * Adds a new public key to the database.
     * @param uuid The UUID of the public key.
     * @param node_name Name of the node.
     * @param ttl TTL of the public key (DB will calculate it NOW() + TTL.
     * @return Returns the number of the deleted keys, this should be always 1 or 0.
     * @throws DatabaseKeyStoreException When SQL exception occurs.
     */
    @Override
    public int addPublicKey(final UUID uuid, final String node_name, final int ttl, final String base64_key) throws DatabaseKeyStoreException {
        try (final Connection conn = workDB.getConnection(); final PreparedStatement ps = conn.prepareStatement(SQL_INSERT_PUBLIC_KEY)) {
            ps.setObject(1, uuid);
            ps.setString(2, node_name);
            ps.setInt(3, ttl);
            ps.setString(4, base64_key);

            final String m = CommonLogMessages.PREFIX_KEY + KeyRelatedMessageElements.PREFIX_DB + KeyRelatedMessageElements.ADDED + " " + KeyRelatedMessageElements.PUBLIC_KEY + " " + uuid;
            logger.info(m);
            return ps.executeUpdate();

        } catch (final SQLException e) {
            final String em = CommonLogMessages.PREFIX_KEY + String.format(ERROR_MESSAGE_FAILED_TO_INSERT_PUBLIC_KEY, uuid.toString(), e.getMessage(), e.getSQLState(), e.getErrorCode());
            logger.error(em);
            throw new DatabaseKeyStoreException();
        }
    }

    /**
     * Deletes a public key from the database based on the given UUID.
     * @return Returns the number of the deleted keys, this should be always 1 or 0.
     * @throws DatabaseKeyStoreException When SQL exception occurs.
     */
    @Override
    public int deletePublicKey(final UUID uuid) throws DatabaseKeyStoreException {
        try (final Connection conn = workDB.getConnection(); final PreparedStatement ps = conn.prepareStatement(SQL_DELETE_PUBLIC_KEY)) {
            ps.setObject(1, uuid);
            final String m = CommonLogMessages.PREFIX_KEY + KeyRelatedMessageElements.PREFIX_DB + KeyRelatedMessageElements.DELETED + " " + KeyRelatedMessageElements.PUBLIC_KEY + " " + uuid;

            logger.info(m);
            return ps.executeUpdate();

        } catch (final SQLException e) {
            final String em = CommonLogMessages.PREFIX_KEY + String.format(ERROR_MESSAGE_FAILED_TO_DELETE_PUBLIC_KEY, uuid.toString(), e.getMessage(), e.getSQLState(), e.getErrorCode());
            logger.error(em);
            throw new DatabaseKeyStoreException();
        }
    }

    /**
     * Fetches the database for a public key.
     * @param uuid The UUID of the desired public key.
     * @return Matching public key with UUID as a compound object. (node_name, ttl, creation_ts, expire_ts, base64_key)
     * @throws DatabaseKeyStoreException When SQL exception occurs.
     * @throws NoSuchElementException When the key does not exist.
     */
    @Override
    public PublicKeyData getPublicKey(final UUID uuid) throws DatabaseKeyStoreException, NoSuchElementException {
        try (final Connection conn = workDB.getConnection(); final PreparedStatement ps = conn.prepareStatement(SQL_SELECT_PUBLIC_KEY)) {
            ps.setObject(1, uuid);
            final ResultSet rs = ps.executeQuery();

            if ( rs != null && rs.next()) {
                final PublicKeyData keyData = new PublicKeyData(
                        uuid,
                        rs.getString("node_name"),
                        rs.getInt("ttl"),
                        rs.getTimestamp("creation_ts"),
                        rs.getTimestamp("expire_ts"),
                        rs.getString("base64_key")
                );

                final String m = CommonLogMessages.PREFIX_KEY + KeyRelatedMessageElements.PREFIX_DB + KeyRelatedMessageElements.FETCHED + " " + KeyRelatedMessageElements.PUBLIC_KEY + " " + keyData;
                logger.info(m);
                return keyData;

            } else {
                final String em = CommonLogMessages.PREFIX_KEY + KeyRelatedMessageElements.PREFIX_DB + KeyRelatedMessageElements.NO_SUCH + " " + KeyRelatedMessageElements.PUBLIC_KEY + " " + uuid;
                logger.info(em);
                throw new NoSuchElementException();
            }

        } catch (final SQLException e) {
            final String em = CommonLogMessages.PREFIX_KEY + String.format(ERROR_MESSAGE_FAILED_TO_SELECT_PUBLIC_KEY, uuid.toString(), e.getMessage(), e.getSQLState(), e.getErrorCode());
            logger.error(em);
            throw new DatabaseKeyStoreException();
        }
    }

    /**
     * Fetches the database for the live public keys.
     * @return Live public keys as a list.
     * @throws DatabaseKeyStoreException When SQL exception occurs.
     */
    @Override
    public List<PublicKeyData> getLivePublicKeys() throws DatabaseKeyStoreException {
        try (final Connection conn = workDB.getConnection(); final PreparedStatement ps = conn.prepareStatement(SQL_SELECT_NON_EXPIRED_PUBLIC_KEYS)) {
            final ResultSet rs = ps.executeQuery();
            final List<PublicKeyData> keys = new LinkedList<>();

            if ( rs != null) {
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
            }

            final String m  = CommonLogMessages.PREFIX_KEY + KeyRelatedMessageElements.PREFIX_DB   + KeyRelatedMessageElements.NUMBER_OF_FETCHED + " " + KeyRelatedMessageElements.PUBLIC_KEYS + " " + KeyRelatedMessageElements.POSTFIX_LIVE + " " + keys.size();
            final String m2 = CommonLogMessages.PREFIX_KEY + KeyRelatedMessageElements.PUBLIC_KEYS + KeyRelatedMessageElements.POSTFIX_LIVE + " " + keys;

            logger.info(m);
            logger.info(m2);
            return keys;

        } catch (final SQLException e) {
            final String errorLog = CommonLogMessages.PREFIX_KEY + String.format(ERROR_MESSAGE_FAILED_TO_SELECT_ACTIVE_PUBLIC_KEYS, e.getMessage(), e.getSQLState(), e.getErrorCode());
            logger.error(errorLog);
            throw new DatabaseKeyStoreException();
        }
    }

    /**
     * Fetches the database for the live public key UUIDs.
     * @return List<UUID> Live key UUIDs as a list.
     * @throws DatabaseKeyStoreException When SQL exception occurs.
     */
    @Override
    public List<UUID> getLivePublicKeyUUIDs() throws DatabaseKeyStoreException {
        try (final Connection conn = workDB.getConnection(); final PreparedStatement ps = conn.prepareStatement(SQL_SELECT_NON_EXPIRED_PUBLIC_KEY_UUIDS)) {
            final ResultSet rs = ps.executeQuery();
            final List<UUID> uuids = new LinkedList<>();

            if ( rs != null) {
                while (rs.next()){
                    uuids.add((UUID) rs.getObject("uuid"));
                }
            }

            final String m  = CommonLogMessages.PREFIX_KEY + KeyRelatedMessageElements.PREFIX_DB + KeyRelatedMessageElements.NUMBER_OF_FETCHED + " " + KeyRelatedMessageElements.PUBLIC_KEY_UUIDS + " " + KeyRelatedMessageElements.POSTFIX_LIVE + " " + uuids.size();
            final String m2 = CommonLogMessages.PREFIX_KEY + KeyRelatedMessageElements.PREFIX_DB + KeyRelatedMessageElements.FETCHED           + " " + KeyRelatedMessageElements.PUBLIC_KEY_UUIDS + " " + KeyRelatedMessageElements.POSTFIX_LIVE + " " + uuids;
            logger.info(m);
            logger.info(m2);
            return uuids;

        } catch (final SQLException e) {
            final String em = CommonLogMessages.PREFIX_KEY + String.format(ERROR_MESSAGE_FAILED_TO_SELECT_EXPIRED_PUBLIC_KEY_UUIDS, e.getMessage(), e.getSQLState(), e.getErrorCode());
            logger.error(em);
            throw new DatabaseKeyStoreException();
        }
    }

    /**
     * Fetches the database for the expired keys UUIDs.
     * @return List<UUID> List of the expired key UUIDs.
     * @throws DatabaseKeyStoreException When SQL exception occurs.
     */
    @Override
    public List<UUID> getExpiredPublicKeyUUIDs() throws DatabaseKeyStoreException {
        try (final Connection conn = workDB.getConnection(); final PreparedStatement ps = conn.prepareStatement(SQL_SELECT_EXPIRED_PUBLIC_KEY_UUIDS)) {
            final ResultSet rs = ps.executeQuery();
            final List<UUID> expired_uuids = new ArrayList<>();

            if ( rs != null){
                while (rs.next()){
                    expired_uuids.add((UUID) rs.getObject("uuid"));
                }
            }

            final String m  = CommonLogMessages.PREFIX_KEY + KeyRelatedMessageElements.PREFIX_DB + KeyRelatedMessageElements.NUMBER_OF_FETCHED + " " + KeyRelatedMessageElements.PUBLIC_KEY_UUIDS + " " + KeyRelatedMessageElements.POSTFIX_EXPIRED + " " + expired_uuids.size();
            final String m2 = CommonLogMessages.PREFIX_KEY + KeyRelatedMessageElements.PREFIX_DB + KeyRelatedMessageElements.FETCHED           + " " + KeyRelatedMessageElements.PUBLIC_KEY_UUIDS + " " + KeyRelatedMessageElements.POSTFIX_EXPIRED + " " + expired_uuids;

            logger.info(m);
            logger.info(m2);
            return expired_uuids;

        } catch (final SQLException e) {
            final String em = CommonLogMessages.PREFIX_KEY + String.format(ERROR_MESSAGE_FAILED_TO_SELECT_EXPIRED_PUBLIC_KEY_UUIDS, e.getMessage(), e.getSQLState(), e.getErrorCode());
            logger.error(em);
            throw new DatabaseKeyStoreException();
        }
    }

    /**
     * Executes an SQL query to remove all the expired key. (expired_ts < NOW() (NOW() is at postgres, not calculated here)
     * @return The number of deleted expired keys.
     * @throws DatabaseKeyStoreException When SQL exception occurs.
     */
    @Override
    public int deleteExpiredPublicKeys() throws DatabaseKeyStoreException {
        try (final Connection conn = workDB.getConnection(); final PreparedStatement ps = conn.prepareStatement(SQL_DELETE_EXPIRED_PUBLIC_KEYS)) {
            final int deletes = ps.executeUpdate();
            final String m = CommonLogMessages.PREFIX_KEY + KeyRelatedMessageElements.PREFIX_DB + KeyRelatedMessageElements.DELETED + " " + KeyRelatedMessageElements.PUBLIC_KEYS + " " + KeyRelatedMessageElements.POSTFIX_EXPIRED + " " + deletes;
            logger.info(m);
            return deletes;

        } catch (final SQLException e) {
            final String em = CommonLogMessages.PREFIX_KEY + String.format(ERROR_MESSAGE_FAILED_TO_DELETE_EXPIRED_PUBLIC_KEYS, e.getMessage(), e.getSQLState(), e.getErrorCode());
            logger.error(em);
            throw new DatabaseKeyStoreException();
        }
    }
}
