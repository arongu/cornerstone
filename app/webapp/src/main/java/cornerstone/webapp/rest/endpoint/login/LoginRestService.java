package cornerstone.webapp.rest.endpoint.login;

import cornerstone.webapp.rest.endpoint.account.AccountEmailPassword;
import cornerstone.webapp.service.account.administration.AccountManager;
import cornerstone.webapp.service.account.administration.exceptions.AccountDoesNotExistException;
import cornerstone.webapp.service.account.administration.exceptions.AccountEmailNotVerifiedException;
import cornerstone.webapp.service.account.administration.exceptions.AccountLockedException;
import cornerstone.webapp.service.account.administration.exceptions.SqlException;
import cornerstone.webapp.service.jwt.JWTService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
            SqlException {

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
