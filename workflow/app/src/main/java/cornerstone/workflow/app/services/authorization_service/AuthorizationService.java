package cornerstone.workflow.app.services.authorization_service;

public interface AuthorizationService {
    String issueJWT(final String emailAddress) throws AuthorizationServiceException;
    // TODO rotate key?
    // TODO prevent brute force attempts on API with login
}
