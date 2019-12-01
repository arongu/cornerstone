package cornerstone.workflow.app.services.login_service;

public interface LoginService {
    boolean authenticate(final String email, final String password);
    String issueJWT(final String email);
}
