package cornerstone.webapp.services.jwt;

public interface AuthorizationServiceInterface {
    String issueJWT(final String emailAddress) throws AuthorizationServiceException;
    // TODO rotate key?
    // TODO prevent brute force attempts on API with login
}
