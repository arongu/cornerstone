package cornerstone.workflow.app.rest.endpoint.admin;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AccountDTO {
    @JsonProperty
    private String email;
    @JsonProperty
    private String password;

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
