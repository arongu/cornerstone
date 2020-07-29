package cornerstone.webapp.services.jwt;

public interface AuthorizationService {
    String issueJWT(final String emailAddress) throws AuthorizationServiceException;
}
