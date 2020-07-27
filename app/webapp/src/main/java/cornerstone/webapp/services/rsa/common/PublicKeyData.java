package cornerstone.webapp.services.rsa.common;

import java.sql.Timestamp;
import java.util.UUID;

public class PublicKeyData {
    private UUID uuid;
    private int ttl;
    private String node_name;
    private String base64_key;
    private Timestamp creation_ts;
    private Timestamp expire_ts;

    public PublicKeyData(final UUID uuid,
                         final String node_name,
                         final int ttl,
                         final Timestamp creation_ts,
                         final Timestamp expire_ts,
                         final String base64_key) {

        this.uuid = uuid;
        this.ttl = ttl;
        this.node_name = node_name;
        this.creation_ts = creation_ts;
        this.expire_ts = expire_ts;
        this.base64_key = base64_key;
    }

    public UUID getUUID() {
        return uuid;
    }

    public void setUUID(final UUID uuid) {
        this.uuid = uuid;
    }

    public int getTTL() {
        return ttl;
    }

    public void setTTL(int ttl) {
        this.ttl = ttl;
    }

    public String getNodeName(){
        return node_name;
    }

    public void setNodeName(final String node_name){
        this.node_name = node_name;
    }

    public String getBase64Key() {
        return base64_key;
    }

    public void setBase64Key(final String base64_key) {
        this.base64_key = base64_key;
    }

    public Timestamp getCreationDate() {
        return creation_ts;
    }

    public void setCreationDate(final Timestamp creationDate) {
        this.creation_ts = creationDate;
    }

    public Timestamp getExpireDate() {
        return expire_ts;
    }

    public void setExpireDate(final Timestamp expireDate) {
        this.expire_ts = expireDate;
    }

    @Override
    public String toString() {
        return "{uuid=" + uuid + ", node_name=" + node_name + ", ttl=" + ttl + ", creation_ts=" + creation_ts + ", expire_ts=" + expire_ts + "}";
    }
}
