package cornerstone.webapp.rest.api.pubkeys;

public class PublicKeyDTO {
    private String pubkey;

    public PublicKeyDTO() {
    }

    public PublicKeyDTO(final String pubkey) {
        this.pubkey = pubkey;
    }

    public String getPubkey() {
        return pubkey;
    }

    public void setPubkey(final String pubkey) {
        this.pubkey = pubkey;
    }
}
