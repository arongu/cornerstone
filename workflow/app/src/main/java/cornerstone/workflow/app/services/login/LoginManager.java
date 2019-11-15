package cornerstone.workflow.app.services.login;

public interface LoginManager {
    boolean authenticate(final String email, final String password);
    String issueJWTtoken(final String email);
}
