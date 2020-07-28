package cornerstone.webapp.rest.endpoint.login;

import cornerstone.webapp.rest.endpoint.account.EmailAndPassword;
import cornerstone.webapp.rest.exceptions.BadRequestException;
import cornerstone.webapp.rest.util.HttpMessage;
import cornerstone.webapp.services.account.administration.AccountManagerException;
import cornerstone.webapp.services.account.administration.AccountManagerInterface;
import cornerstone.webapp.services.jwt.AuthorizationServiceException;
import cornerstone.webapp.services.jwt.AuthorizationServiceInterface;
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

    private final AccountManagerInterface accountAdmin;
    private final AuthorizationServiceInterface authorizationService;

    @Inject
    public LoginRestService(final AccountManagerInterface accountAdmin, final AuthorizationServiceInterface authorizationService) {
        this.accountAdmin = accountAdmin;
        this.authorizationService = authorizationService;
    }

    @POST
    public Response authenticateUser(final EmailAndPassword emailAndPassword) throws AccountManagerException, AuthorizationServiceException, BadRequestException {
        if (null != emailAndPassword &&
            null != emailAndPassword.email &&
            null != emailAndPassword.password ) {

            final Response response;
            final boolean authenticated;

            authenticated = accountAdmin.login(emailAndPassword.email, emailAndPassword.password);
            if ( authenticated ) {
                final String jwt = authorizationService.issueJWT(emailAndPassword.email);
                response = Response.status(Response.Status.ACCEPTED).entity(jwt).build();
                logger.info("[ NEW ACCESS TOKEN ][ GRANTED ] -- '{}'", emailAndPassword.email);

            } else {
                final HttpMessage httpMessage = new HttpMessage(Response.Status.FORBIDDEN.toString(), Response.Status.FORBIDDEN.getStatusCode());
                response = Response.status(Response.Status.FORBIDDEN).entity(httpMessage).build();
                logger.info("[ NEW ACCESS TOKEN ][ DENIED ] -- '{}'", emailAndPassword.email);
            }

            return response;

        } else {
            throw new BadRequestException();
        }
    }
}
