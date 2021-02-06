package cornerstone.webapp.services.jwt;

import cornerstone.webapp.services.keys.stores.local.SigningKeysException;

import java.util.Map;

public interface JWTService {
    /**
     * Creates JWS for the subject.
     * @param subject Subject of the JWS.
     */
    String createJws(final String subject) throws SigningKeysException;

    /**
     * Creates JWS for the subject with the given Map as claims.
     * @param subject Subject of the JWS.
     * @param claimsMap A map which will be added to the JWS as claims.
     * @return JWS as a bas64 string.
     * @throws SigningKeysException Throws it if the JWT cannot be signed, with the private key.
     */
    String createJws(final String subject, final Map<String,Object> claims) throws SigningKeysException;
}
