package cornerstone.webapp.services.rsa.store.db;

public class DbPublicKeyStoreException extends Exception {
    public DbPublicKeyStoreException(){
        super();
    }

    public DbPublicKeyStoreException(final String message) {
        super(message);
    }
}
