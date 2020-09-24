package cornerstone.webapp.rest.endpoints.login;

public class TokenDTO {
    private String token;

    public TokenDTO() {
    }

    public TokenDTO(final String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
