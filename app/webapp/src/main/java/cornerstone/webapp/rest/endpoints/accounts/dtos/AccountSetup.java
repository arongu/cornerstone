package cornerstone.webapp.rest.endpoints.accounts.dtos;

import cornerstone.webapp.services.account.management.AccountRole;

public class AccountSetup {
    private String email;
    private String password;
    private boolean locked;
    private boolean verified;
    private AccountRole accountRole;

    public AccountSetup() {
    }

    public AccountSetup(final String email, final String password,
                        final boolean locked, final boolean verified,
                        final AccountRole accountRole) {

        this.email = email;
        this.password = password;
        this.locked = locked;
        this.verified = verified;
        this.accountRole = accountRole;
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

    public AccountRole getAccountRole() {
        return accountRole;
    }

    public void setAccountRole(final AccountRole accountRole) {
        this.accountRole = accountRole;
    }
}
