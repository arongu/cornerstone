package cornerstone.workflow.app.rest.endpoint.login;

import cornerstone.workflow.app.services.login.LoginManager;
import cornerstone.workflow.app.rest.endpoint.admin.AccountDTO;
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

    private final LoginManager loginManager;

    @Inject
    public LoginRestService(final LoginManager loginManager) {
        this.loginManager = loginManager;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response authenticateUser(final AccountDTO accountDTO) {
        logger.info("FROM JSON: email {}, password {}", accountDTO.getEmail(), accountDTO.getPassword());

        try {
            boolean b = loginManager.authenticate(accountDTO.getEmail(), accountDTO.getPassword());
            logger.info("auth: {}", b);
            if ( b ){
                String token = loginManager.issueJWTtoken(accountDTO.getEmail());
                logger.info("token: {}", token);
                return Response.status(Response.Status.ACCEPTED).entity(token).build();
            }
            else {
                return Response.status(Response.Status.FORBIDDEN).entity("access denied").build();
            }
        }
        catch (Exception e) {
            logger.error(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
}
