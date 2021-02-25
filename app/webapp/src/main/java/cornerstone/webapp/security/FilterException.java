package cornerstone.webapp.security;

import io.jsonwebtoken.io.IOException;

public class FilterException extends IOException {
    public FilterException(String msg) {
        super(msg);
    }
}
