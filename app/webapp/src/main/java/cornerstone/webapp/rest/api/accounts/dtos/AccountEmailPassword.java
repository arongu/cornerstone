package cornerstone.webapp.rest.api.accounts.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AccountEmailPassword {
    @JsonProperty(required = true)
    private String email;
    @JsonProperty(required = true)
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
