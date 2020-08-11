package cornerstone.webapp.service.jwt;

import java.util.Map;

public interface JWTService {
    String issueJWT(final String email);
    String issueJWT(final String email, final Map<String,Object> claims);
}
