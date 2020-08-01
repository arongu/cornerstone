package cornerstone.webapp.services.rsa.store.log;

public class MessageElements {
    // key types
    public static final String PUBLIC_KEY             = "PUBLIC KEY";
    public static final String PUBLIC_KEY_UUIDS       = "PUBLIC KEY UUIDS";
    public static final String PUBLIC_AND_PRIVATE_KEY = "PUBLIC AND PRIVATE KEY";

    // prefixes
    public static final String DB_PREFIX    = "(DB) ";
    public static final String PREFIX_LOCAL = "(LOCAL) ";

    // postfixes
    public static final String POST_FIX_LIVE    = " (LIVE)";
    public static final String POST_FIX_EXPIRED = " (EXPIRED)";

    // db
    public static final String DB_NUMBER_OF_FETCHED = "NUMBER OF FETCHED";

    // operations
    public static final String ADDED   = "ADDED";
    public static final String DELETED = "DELETED";
    public static final String FETCHED = "FETCHED";
    public static final String KEPT    = "KEPT";
    public static final String NO_SUCH = "NO SUCH";
    public static final String NOT_SET = "NOT SET";
    public static final String SET     = "SET";
    public static final String SYNC    = "SYNC";
}
