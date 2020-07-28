package cornerstone.webapp.services.jwt;

public interface AuthorizationServiceInterface {
    // TODO prevent brute force attempts on API with login
    String issueJWT(final String emailAddress) throws AuthorizationServiceException;
}
