package cornerstone.workflow.webapp.services.rsa_key_services.local;

import cornerstone.workflow.webapp.logmessages.ServiceLogMessages;
import cornerstone.workflow.webapp.configuration.ConfigurationLoader;
import cornerstone.workflow.webapp.services.rsa_key_services.db.PublicKeyStorageServiceInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

public class KeyService implements KeyServiceInterface {
    private static final Logger logger = LoggerFactory.getLogger(KeyService.class);
    private PublicKeyStorageServiceInterface publicKeyStorageService;
    private PrivateKey signingKey;
    private UUID publicKeyUUID;
    private int ttl;
    private Map<UUID, PublicKeyWithTTL> publicKeyMap;

    @Inject
    public KeyService(final ConfigurationLoader configurationLoader, final PublicKeyStorageServiceInterface publicKeyStorageService) {
        this.publicKeyMap =  new HashMap<>();
        // error handling
        // get if exits otherwise dunno or fail before it gets here this.ttl = configurationLoader.getApp_properties().getProperty(APP_ENUM.APP_RSA_KEY_TTL.key);
        logger.info(String.format(ServiceLogMessages.MESSAGE_INSTANCE_CREATED, this.getClass().getName()));
    }

    @Override
    public void setSigningKeyTTL(int ttl) {
        this.ttl = ttl;
    }

    @Override
    public void resetSigningKey() {
        final KeyPairWithUUID keys = new KeyPairWithUUID();


    }

    @Override
    public void storePublicKey(UUID uuid, String base64_key) {

    }

    @Override
    public void storePublicKey(UUID uuid, PublicKey publicKey) {

    }

    @Override
    public PrivateKey getSigningKey() {
        return null;
    }

    @Override
    public PublicKey getPublicKey(UUID uuid) throws NoSuchElementException {
        return null;
    }
}
