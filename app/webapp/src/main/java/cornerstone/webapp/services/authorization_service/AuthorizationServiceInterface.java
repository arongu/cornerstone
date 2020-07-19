package cornerstone.webapp.services.authorization_service;

public interface AuthorizationServiceInterface {
    String issueJWT(final String emailAddress) throws AuthorizationServiceException;
    // TODO rotate key?
    // TODO prevent brute force attempts on API with login
}
