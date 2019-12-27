package cornerstone.workflow.app.services.authorization_service;

public interface AuthorizationService {
    String issueJWT(final String emailAddress) throws AuthorizationServiceException;
}
