package cornerstone.workflow.app.services.login_service;

public interface LoginService {
    boolean authenticate(final String emailAddress, final String password);
    String issueJWT(final String emailAddress);
    // TODO String issueJWT(final String email, final Long seconds)
    // TODO String issueJWT(final String email, final Date expDate)
}
