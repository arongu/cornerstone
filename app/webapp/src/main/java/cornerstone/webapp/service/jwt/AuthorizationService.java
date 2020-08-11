package cornerstone.webapp.service.jwt;

public interface AuthorizationService {
    String issueJWT(final String emailAddress) throws AuthorizationServiceException;
}
