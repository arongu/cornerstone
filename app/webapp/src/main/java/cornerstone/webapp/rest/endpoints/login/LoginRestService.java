package cornerstone.webapp.rest.endpoints.login;

import cornerstone.webapp.common.AlignedLogMessages;
import cornerstone.webapp.rest.endpoints.accounts.dtos.AccountEmailPassword;
import cornerstone.webapp.rest.error_responses.ErrorResponse;
import cornerstone.webapp.services.account.management.AccountManager;
import cornerstone.webapp.services.account.management.exceptions.single.LockedException;
import cornerstone.webapp.services.account.management.exceptions.single.NoAccountException;
import cornerstone.webapp.services.account.management.exceptions.single.UnverifiedEmailException;
import cornerstone.webapp.services.jwt.JWTService;
import cornerstone.webapp.services.rsa.store.local.SigningKeySetupException;
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
    public Response authenticateUser(final AccountEmailPassword accountEmailPassword) throws SigningKeySetupException {

        if (null != accountEmailPassword &&
            null != accountEmailPassword.getEmail() &&
            null != accountEmailPassword.getPassword()) {

            final boolean authenticated;

            try {
                authenticated = accountManager.login(accountEmailPassword.getEmail(), accountEmailPassword.getPassword());

            } catch (final LockedException | UnverifiedEmailException | NoAccountException e) {
                final String logMsg = String.format(
                        AlignedLogMessages.FORMAT__OFFSET_35C_C_STR,
                        AlignedLogMessages.OFFSETS_ALIGNED_CLASSES.get(getClass().getName()),
                        e.getMessage(), accountEmailPassword.getEmail()
                );

                logger.info(logMsg);
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(new ErrorResponse(Response.Status.UNAUTHORIZED.getStatusCode(), "Unauthorized."))
                        .build();
            }

            if ( authenticated ) {
                final String jwt = JWTService.createJws(accountEmailPassword.getPassword(), null);
                final String logMsg = String.format(
                        AlignedLogMessages.FORMAT__OFFSET_35C_C_STR,
                        AlignedLogMessages.OFFSETS_ALIGNED_CLASSES.get(getClass().getName()),
                        "TOKEN GRANTED", accountEmailPassword.getEmail()
                );

                logger.info(logMsg);
                return Response.status(Response.Status.OK).entity(new TokenDTO(jwt)).build();

            } else {
                final String logMsg = String.format(
                        AlignedLogMessages.FORMAT__OFFSET_35C_C_STR,
                        AlignedLogMessages.OFFSETS_ALIGNED_CLASSES.get(getClass().getName()),
                        "TOKEN DENIED", accountEmailPassword.getEmail()
                );
                logger.info(logMsg);
            }
        }

        return Response.status(Response.Status.UNAUTHORIZED)
                .entity(new ErrorResponse(Response.Status.UNAUTHORIZED.getStatusCode(), "Unauthorized."))
                .build();
    }
}
