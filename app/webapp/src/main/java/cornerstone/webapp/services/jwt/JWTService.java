package cornerstone.webapp.services.jwt;

import cornerstone.webapp.services.rsa.store.local.SigningKeySetupException;

import java.util.Map;

public interface JWTService {
    String createJws(final String subject) throws SigningKeySetupException;
    String createJws(final String subject, final Map<String,Object> claims) throws SigningKeySetupException;
}
