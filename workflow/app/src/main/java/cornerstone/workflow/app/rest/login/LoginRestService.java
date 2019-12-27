package cornerstone.workflow.app.rest.login;

import cornerstone.workflow.app.rest.account.AccountLoginJsonDto;
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
    public Response authenticateUser(final AccountLoginJsonDto accountLoginJsonDTO)
            throws BadRequestException, AuthorizationServiceException {

        if ( null != accountLoginJsonDTO &&
             null != accountLoginJsonDTO.getEmail() &&
             null != accountLoginJsonDTO.getPassword()) {

            final Response response;
            final boolean authenticated;

            authenticated = authenticationService.authenticate(
                    accountLoginJsonDTO.getEmail(),
                    accountLoginJsonDTO.getPassword()
            );

            if ( authenticated ) {
                response = Response.status(Response.Status.ACCEPTED)
                        .entity(authorizationService.issueJWT(accountLoginJsonDTO.getEmail()))
                        .build();

                logger.info("[ NEW ACCESS TOKEN ][ GRANTED ] -- '{}'", accountLoginJsonDTO.getEmail());

            } else {
                response = Response.status(Response.Status.FORBIDDEN)
                        .entity(
                                new HttpMessage(
                                        Response.Status.FORBIDDEN.toString(),
                                        Response.Status.FORBIDDEN.getStatusCode()
                                )
                        ).build();

                logger.info("[ NEW ACCESS TOKEN ][ DENIED ] -- '{}'", accountLoginJsonDTO.getEmail());
            }

            return response;

        } else {
            throw new BadRequestException();
        }
    }
}
