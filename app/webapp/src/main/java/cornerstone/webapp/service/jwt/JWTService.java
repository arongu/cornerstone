package cornerstone.webapp.service.jwt;

import java.util.Map;

public interface JWTService {
    String createJws(final String email);
    String createJws(final String email, final Map<String,Object> claims);
}
