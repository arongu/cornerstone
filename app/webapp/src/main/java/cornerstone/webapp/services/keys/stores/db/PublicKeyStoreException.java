package cornerstone.webapp.services.keys.stores.db;

public class PublicKeyStoreException extends Exception {
    public PublicKeyStoreException(){
        super();
    }

    public PublicKeyStoreException(final String message) {
        super(message);
    }
}
