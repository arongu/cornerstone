package cornerstone.webapp.services.keys.stores.log;

public class MessageElements {
    // key, keys
    public static final String PUBLIC_KEY             = "PUBLIC KEY";
    public static final String PUBLIC_KEYS            = "PUBLIC KEYS";
    public static final String PUBLIC_KEY_UUIDS       = "PUBLIC KEY UUIDS";
    public static final String PUBLIC_AND_PRIVATE_KEY = "PUBLIC AND PRIVATE KEY";

    // prefixes
    public static final String PREFIX_DB    = "(DB) ";
    public static final String PREFIX_LOCAL = "(LOCAL) ";

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
}
