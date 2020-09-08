package cornerstone.webapp.rest.endpoints.account.dtos;

public class AccountSetup {
    private String email;
    private String password;
    private boolean locked;
    private boolean verified;

    public AccountSetup(String email, String password, boolean locked, boolean verified) {
        this.email = email;
        this.password = password;
        this.locked = locked;
        this.verified = verified;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }
}
