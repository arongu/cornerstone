package cornerstone.webapp.security;

import java.security.Principal;

public class PrincipalImpl implements Principal {
    private final String name;

    public PrincipalImpl(final String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
