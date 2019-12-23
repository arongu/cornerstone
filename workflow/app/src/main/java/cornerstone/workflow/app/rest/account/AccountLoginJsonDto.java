package cornerstone.workflow.app.rest.account;

public class AccountLoginJsonDto {
    private String email, password;

    public AccountLoginJsonDto() {
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
