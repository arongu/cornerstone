package cornerstone.workflow.app.rest.login;

import cornerstone.workflow.app.rest.account.AccountLoginJsonDto;
import cornerstone.workflow.app.rest.rest_exceptions.BadRequestException;
import cornerstone.workflow.app.rest.rest_messages.HttpMessage;
import cornerstone.workflow.app.services.login_service.LoginService;
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

    private final LoginService loginService;

    @Inject
    public LoginRestService(final LoginService loginService) {
        this.loginService = loginService;
    }

    // TODO token message
    @POST
    public Response authenticateUser(final AccountLoginJsonDto AccountLoginJsonDTO) throws BadRequestException {

        if ( null != AccountLoginJsonDTO && null != AccountLoginJsonDTO.getEmail() && null != AccountLoginJsonDTO.getPassword()) {
            final Response response;
            if ( loginService.authenticate(AccountLoginJsonDTO.getEmail(), AccountLoginJsonDTO.getPassword()) ) {
                response = Response.status(Response.Status.ACCEPTED)
                        .entity(loginService.issueJWT(AccountLoginJsonDTO.getEmail()))
                        .build();

                logger.info("[ NEW ACCESS TOKEN ][ GRANTED ] -- '{}'", AccountLoginJsonDTO.getEmail());

            } else {
                response = Response.status(Response.Status.FORBIDDEN)
                        .entity(new HttpMessage(Response.Status.FORBIDDEN.toString(), Response.Status.FORBIDDEN.getStatusCode()))
                        .build();

                logger.info("[ NEW ACCESS TOKEN ][ DENIED ] -- '{}'", AccountLoginJsonDTO.getEmail());
            }

            return response;
        } else {
            throw new BadRequestException();
        }
    }
}
