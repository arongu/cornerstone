package cornerstone.workflow.app.rest.login;

import cornerstone.workflow.app.rest.account.AccountDTO;
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

    @POST
    public Response authenticateUser(final AccountDTO accountDTO) throws BadRequestException {
        if ( null != accountDTO && null != accountDTO.getEmail() && null != accountDTO.getPassword()) {
            if ( loginService.authenticate(accountDTO.getEmail(), accountDTO.getPassword()) ) {
                logger.info("[ NEW ACCESS TOKEN ][ GRANTED ] -- '{}'", accountDTO.getEmail());

                return Response.status(Response.Status.ACCEPTED)
                        .entity(loginService.issueJWT(accountDTO.getEmail()))
                        .build();

            } else {
                logger.info("[ NEW ACCESS TOKEN ][ DENIED ] -- '{}'", accountDTO.getEmail());

                return Response.status(Response.Status.FORBIDDEN)
                        .entity(new HttpMessage(Response.Status.FORBIDDEN.toString(), Response.Status.FORBIDDEN.getStatusCode()))
                        .build();
            }
        } else {
            throw new BadRequestException();
        }
    }
}
