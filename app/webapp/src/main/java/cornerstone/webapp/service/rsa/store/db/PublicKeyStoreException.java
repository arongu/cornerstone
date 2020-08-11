package cornerstone.webapp.service.rsa.store.db;

public class PublicKeyStoreException extends Exception {
    public PublicKeyStoreException(){
        super();
    }

    public PublicKeyStoreException(final String message) {
        super(message);
    }
}
