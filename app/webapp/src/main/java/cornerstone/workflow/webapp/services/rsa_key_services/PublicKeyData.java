package cornerstone.workflow.webapp.services.rsa_key_services;

import java.sql.Timestamp;
import java.util.UUID;

public class PublicKeyData {
    private UUID uuid;
    private String base64_key;
    private Timestamp expireDate;

    public PublicKeyData(final UUID uuid,final String base64_key,final Timestamp expireDate) {
        this.uuid = uuid;
        this.base64_key = base64_key;
        this.expireDate = expireDate;
    }

    public UUID getUUID() {
        return uuid;
    }

    public void setUUID(final UUID uuid) {
        this.uuid = uuid;
    }

    public String getBase64Key() {
        return base64_key;
    }

    public void setBase64Key(final String base64_key) {
        this.base64_key = base64_key;
    }

    public Timestamp getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(final Timestamp expireDate) {
        this.expireDate = expireDate;
    }
}
