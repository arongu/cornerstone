package cornerstone.webapp.rest.endpoint.login;

import cornerstone.webapp.rest.endpoint.account.AccountEmailPassword;
import cornerstone.webapp.service.account.administration.AccountManager;
import cornerstone.webapp.service.account.administration.exceptions.AccountDoesNotExistException;
import cornerstone.webapp.service.account.administration.exceptions.AccountEmailNotVerifiedException;
import cornerstone.webapp.service.account.administration.exceptions.AccountLockedException;
import cornerstone.webapp.service.account.administration.exceptions.AccountManagerSqlException;
import cornerstone.webapp.service.jwt.JWTService;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Path("/login")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class LoginRestService {
    private static final Logger logger = LoggerFactory.getLogger(LoginRestService.class);

    private final AccountManager accountAdmin;
    private final JWTService JWTService;

    @Inject
    public LoginRestService(final AccountManager accountAdmin, final JWTService JWTService) {
        this.accountAdmin = accountAdmin;
        this.JWTService = JWTService;
    }

    @POST
    public Response authenticateUser(final AccountEmailPassword accountEmailPassword) throws
            AccountEmailNotVerifiedException,
            AccountLockedException,
            AccountDoesNotExistException,
            AccountManagerSqlException {

        if (null != accountEmailPassword &&
            null != accountEmailPassword.getEmail() &&
            null != accountEmailPassword.getPassword()) {

            final boolean authenticated;

            authenticated = accountAdmin.login(accountEmailPassword.getEmail(), accountEmailPassword.getPassword());
            if ( authenticated ) {
                final String jwt = JWTService.createJws(accountEmailPassword.getPassword(), null);
                logger.info("[ ACCESS TOKEN ][ GRANTED ] -- '{}'", accountEmailPassword.getEmail());
                return Response.status(Response.Status.ACCEPTED).entity(jwt).build();
            } else {
                logger.info("[ ACCESS TOKEN ][ DENIED ] -- '{}'", accountEmailPassword.getEmail());
            }
        }

        return Response.status(Response.Status.FORBIDDEN).entity("Access denied").build();
    }
}
