package cornerstone.webapp.services.jwt;

import cornerstone.webapp.services.keys.stores.local.SigningKeysException;

import java.util.Map;

public interface JWTService {
    String createJws(final String subject) throws SigningKeysException;
    String createJws(final String subject, final Map<String,Object> claims) throws SigningKeysException;
}
