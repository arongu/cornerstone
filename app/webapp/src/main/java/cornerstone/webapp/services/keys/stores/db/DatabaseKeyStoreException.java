package cornerstone.webapp.services.keys.stores.db;

public class DatabaseKeyStoreException extends Exception {
    public DatabaseKeyStoreException(){
        super();
    }

    public DatabaseKeyStoreException(final String message) {
        super(message);
    }
}
