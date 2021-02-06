package cornerstone.webapp.rest.endpoints.login;

import cornerstone.webapp.logmsg.AlignedLogMessages;
import cornerstone.webapp.rest.endpoints.accounts.dtos.AccountEmailPassword;
import cornerstone.webapp.rest.error_responses.ErrorResponse;
import cornerstone.webapp.services.accounts.management.AccountManager;
import cornerstone.webapp.services.accounts.management.AccountResultSet;
import cornerstone.webapp.services.accounts.management.exceptions.single.*;
import cornerstone.webapp.services.jwt.JWTService;
import cornerstone.webapp.services.keys.stores.local.SigningKeysException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

/**
 * Creates a signed JWT token and sends it back to the user.
 */

@Singleton
@PermitAll
@Path("/login")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class LoginRestService {
    private static final Logger logger = LoggerFactory.getLogger(LoginRestService.class);

    private final AccountManager accountManager;
    private final JWTService JWTService;

    @Inject
    public LoginRestService(final AccountManager accountManager, final JWTService JWTService) {
        this.accountManager = accountManager;
        this.JWTService = JWTService;
    }

    @POST
    public Response login(final AccountEmailPassword accountEmailPassword) throws SigningKeysException {
        if ( accountEmailPassword == null || accountEmailPassword.getEmail() == null || accountEmailPassword.getPassword() == null ) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(Response.Status.BAD_REQUEST.getStatusCode(), "Null value provided for email/password."))
                    .build();
        }

        final AccountResultSet accountResultSet;
        try {
             accountResultSet = accountManager.login(accountEmailPassword.getEmail(), accountEmailPassword.getPassword());

        } catch (final LockedException | UnverifiedEmailException | NoAccountException | BadPasswordException | RetrievalException e) {
            final ErrorResponse errorResponse;
            final Response.Status responseStatus;
            final String logMsg;

            logMsg = String.format(
                    AlignedLogMessages.FORMAT__OFFSET_35C_C_35C_C_STR,
                    AlignedLogMessages.OFFSETS_ALIGNED_CLASSES.get(getClass().getName()),
                    "TOKEN DENIED", accountEmailPassword.getEmail(), e.getMessage()
            );


            if ( e instanceof RetrievalException ) {
                logger.error(logMsg);
                responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
                errorResponse = new ErrorResponse(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), e.getMessage());

            } else {
                logger.info(logMsg);
                responseStatus = Response.Status.UNAUTHORIZED;
                errorResponse = new ErrorResponse(Response.Status.UNAUTHORIZED.getStatusCode(), "Unauthorized.");
            }

            return Response.status(responseStatus.getStatusCode()).entity(errorResponse).build();
        }


        final Map<String, Object> claims = new HashMap<>();
        claims.put("role", accountResultSet.role_name);

        final String jwt = JWTService.createJws(accountEmailPassword.getPassword(), claims);
        final String logMsg = String.format(
                AlignedLogMessages.FORMAT__OFFSET_35C_C_35C_C_STR,
                AlignedLogMessages.OFFSETS_ALIGNED_CLASSES.get(getClass().getName()),
                "TOKEN GRANTED", accountEmailPassword.getEmail(), claims
        );

        logger.info(logMsg);
        return Response.status(Response.Status.OK).entity(new TokenDTO(jwt)).build();
    }
}
