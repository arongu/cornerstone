package cornerstone.workflow.app.rest.login;

import cornerstone.workflow.app.rest.account.EmailPasswordPair;
import cornerstone.workflow.app.rest.rest_exceptions.BadRequestException;
import cornerstone.workflow.app.rest.rest_messages.HttpMessage;
import cornerstone.workflow.app.services.authentication_service.AuthenticationService;
import cornerstone.workflow.app.services.authorization_service.AuthorizationService;
import cornerstone.workflow.app.services.authorization_service.AuthorizationServiceException;
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
import javax.ws.rs.ext.Provider;

@Provider
@Singleton
@Path("/login")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class LoginRestService {
    private static final Logger logger = LoggerFactory.getLogger(LoginRestService.class);

    private final AuthenticationService authenticationService;
    private final AuthorizationService authorizationService;

    @Inject
    public LoginRestService(final AuthenticationService authenticationService,
                            final AuthorizationService authorizationService) {

        this.authenticationService = authenticationService;
        this.authorizationService = authorizationService;
    }

    // TODO token message
    @POST
    public Response authenticateUser(final EmailPasswordPair JSONemailPassword)
            throws BadRequestException, AuthorizationServiceException {

        if ( null != JSONemailPassword &&
             null != JSONemailPassword.getEmail() &&
             null != JSONemailPassword.getPassword()) {

            final Response response;
            final boolean authenticated;

            authenticated = authenticationService.authenticate(
                    JSONemailPassword.getEmail(),
                    JSONemailPassword.getPassword()
            );

            if ( authenticated ) {
//                TODO fix this mess message is not in JSON format
                final String jwt = authorizationService.issueJWT(JSONemailPassword.getEmail());
                response = Response.status(Response.Status.ACCEPTED)
                        .entity(jwt)
                        .build();

                logger.info("[ NEW ACCESS TOKEN ][ GRANTED ] -- '{}'", JSONemailPassword.getEmail());

            } else {
                response = Response.status(Response.Status.FORBIDDEN)
                        .entity(
                                new HttpMessage(
                                        Response.Status.FORBIDDEN.toString(),
                                        Response.Status.FORBIDDEN.getStatusCode()
                                )
                        ).build();

                logger.info("[ NEW ACCESS TOKEN ][ DENIED ] -- '{}'", JSONemailPassword.getEmail());
            }

            return response;

        } else {
            throw new BadRequestException();
        }
    }
}
