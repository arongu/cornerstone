package cornerstone.webapp.services.keys.stores.logging;

public class MessageElements {
    // key, keys
    public static final String PUBLIC_KEY             = "PUBLIC KEY";
    public static final String PUBLIC_KEYS            = "PUBLIC KEYS";
    public static final String PUBLIC_KEY_UUIDS       = "PUBLIC KEY UUIDS";
    public static final String PUBLIC_AND_PRIVATE_KEY = "PUBLIC AND PRIVATE KEY";

    // prefixes
    public static final String PREFIX_DB      = "(DB) ";
    public static final String PREFIX_LOCAL   = "(LOCAL) ";
    public static final String PREFIX_MANAGER = "(MANAGER) ";

    // postfixes
    public static final String POSTFIX_EXPIRED = " (EXPIRED)";
    public static final String POSTFIX_LIVE    = " (LIVE)";

    // only db
    public static final String NUMBER_OF_FETCHED = "NUMBER OF FETCHED";
    public static final String FETCHED           = "FETCHED";

    // only local
    public static final String NOT_SET = "NOT SET";
    public static final String SET     = "SET";
    public static final String SYNC    = "SYNC";

    // both
    public static final String ADDED   = "ADDED";
    public static final String DELETED = "DELETED";
    public static final String KEPT    = "KEPT";
    public static final String NO_SUCH = "NO SUCH";

    // manager
    public static final String ADDING                            = "ADDING ";
    public static final String DELETING                          = "DELETING ";
    public static final String EXPIRED_KEYS                      = "EXPIRED KEYS";
    public static final String FETCHING                          = "FETCHING ";

    // stores
    public static final String DATABASE_KEYSTORE_ERROR           = "DATABASE KEYSTORE ERROR";

    // cache add
    public static final String PUBLIC_KEY_ADDED_FOR_REINSERT     = "PUBLIC KEY ADDED TO RE-INSERT";
    public static final String PUBLIC_KEY_REMOVED_FROM_REINSERT  = "PUBLIC KEY REMOVED FROM RE-INSERT";

    // cache remove
    public static final String PUBLIC_KEY_ADDED_TO_RE_DELETE     = "PUBLIC KEY ADDED TO RE-DELETE";
    public static final String PUBLIC_KEY_REMOVED_FROM_RE_DELETE = "PUBLIC KEY REMOVED FROM RE-DELETE";

    // conversion, invalid
    public static final String PUBLIC_KEY_CONVERSION_ERROR                  = "PUBLIC KEY BASE64 CONVERSION ERROR";
    public static final String PUBLIC_KEY_IS_INVALID                        = "PUBLIC KEY IS INVALID";
    public static final String PUBLIC_KEY_RECEIVED_FROM_DATABASE_IS_CORRUPT = "PUBLIC KEY RECEIVED FROM DATABASE IS CORRUPT";
}
