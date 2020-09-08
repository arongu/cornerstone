package cornerstone.webapp.services.jwt;

import java.util.Map;

public interface JWTService {
    String createJws(final String subject);
    String createJws(final String subject, final Map<String,Object> claims);
}
