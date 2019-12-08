package cornerstone.workflow.app.rest.login;

import cornerstone.workflow.app.rest.account.AccountDTO;
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

@Singleton
@Path("/login")
public class LoginRestService {
    private static final Logger logger = LoggerFactory.getLogger(LoginRestService.class);

    private final LoginService loginService;

    @Inject
    public LoginRestService(final LoginService loginService) {
        this.loginService = loginService;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response authenticateUser(final AccountDTO accountDTO) {
        logger.info("FROM JSON: email {}, password {}", accountDTO.getEmail(), accountDTO.getPassword());

        try {
            final boolean authenticated = loginService.authenticate(accountDTO.getEmail(), accountDTO.getPassword());
            logger.info("auth: {}", authenticated);

            if ( authenticated ){
                final String token = loginService.issueJWT(accountDTO.getEmail());
                logger.info("token: {}", token);

                return Response.status(Response.Status.ACCEPTED)
                        .entity(token)
                        .build();
            }
            else {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity(Response.Status.FORBIDDEN.toString())
                        .build();
            }
        } catch (final Exception e) {
            logger.error(e.getMessage());
            return Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(e.getMessage())
                    .build();
        }
    }
}
