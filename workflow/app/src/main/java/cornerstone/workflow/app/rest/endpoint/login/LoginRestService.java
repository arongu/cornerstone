package cornerstone.workflow.app.rest.endpoint.login;

import cornerstone.workflow.app.rest.endpoint.account.EmailAndPassword;
import cornerstone.workflow.app.rest.exceptions.BadRequestException;
import cornerstone.workflow.app.rest.util.HttpMessage;
import cornerstone.workflow.app.services.account_service.AccountServiceInterface;
import cornerstone.workflow.app.services.account_service.AccountServiceException;
import cornerstone.workflow.app.services.authorization_service.AuthorizationServiceInterface;
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

    private AccountServiceInterface accountServiceInterface;
    private AuthorizationServiceInterface authorizationServiceInterface;

    @Inject
    public LoginRestService(final AccountServiceInterface accountServiceInterface,
                            final AuthorizationServiceInterface authorizationServiceInterface) {

        this.accountServiceInterface = accountServiceInterface;
        this.authorizationServiceInterface = authorizationServiceInterface;
    }

    @POST
    public Response authenticateUser(final EmailAndPassword emailAndPassword) throws AccountServiceException,
                                                                                     AuthorizationServiceException,
                                                                                     BadRequestException {

        if ( null != emailAndPassword &&
             null != emailAndPassword.email &&
             null != emailAndPassword.password ) {

            final Response response;
            final boolean authenticated;

            authenticated = accountServiceInterface.login(
                    emailAndPassword.email,
                    emailAndPassword.password
            );

            if ( authenticated ) {
                final String jwt = authorizationServiceInterface.issueJWT(emailAndPassword.email);

                response = Response
                        .status(Response.Status.ACCEPTED)
                        .entity(jwt)
                        .build();

                logger.info("[ NEW ACCESS TOKEN ][ GRANTED ] -- '{}'", emailAndPassword.email);

            } else {
                final HttpMessage httpMessage = new HttpMessage(
                        Response.Status.FORBIDDEN.toString(),
                        Response.Status.FORBIDDEN.getStatusCode()
                );

                response = Response
                        .status(Response.Status.FORBIDDEN)
                        .entity(httpMessage)
                        .build();

                logger.info("[ NEW ACCESS TOKEN ][ DENIED ] -- '{}'", emailAndPassword.email);
            }

            return response;

        } else {
            throw new BadRequestException();
        }
    }
}
