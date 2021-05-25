package cornerstone.webapp.rest.api.accounts.dtos;

public class AccountSetup {
    private String  email;
    private String  password;
    private String  role;
    private boolean locked;
    private boolean verified;

    public AccountSetup() {
    }

    public AccountSetup(final String email, final String password,
                        final boolean locked, final boolean verified,
                        final String role) {

        this.email    = email;
        this.password = password;
        this.locked   = locked;
        this.verified = verified;
        this.role     = role;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(final boolean locked) {
        this.locked = locked;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(final boolean verified) {
        this.verified = verified;
    }

    public String getRole() {
        return role;
    }

    public void setRole(final String role) {
        this.role = role;
    }
}
