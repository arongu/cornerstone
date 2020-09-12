package cornerstone.webapp.rest.endpoints.accounts.dtos;

public class AccountEmailPassword {
    private String email;
    private String password;

    public AccountEmailPassword() {
    }

    public AccountEmailPassword(final String email, final String password) {
        this.email = email;
        this.password = password;
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
}
