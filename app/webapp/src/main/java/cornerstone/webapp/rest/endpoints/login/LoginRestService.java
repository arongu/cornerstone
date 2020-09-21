package cornerstone.webapp.rest.endpoints.login;

import cornerstone.webapp.common.AlignedLogMessages;
import cornerstone.webapp.rest.endpoints.accounts.dtos.AccountEmailPassword;
import cornerstone.webapp.rest.error_responses.SingleErrorResponse;
import cornerstone.webapp.services.account.administration.AccountManager;
import cornerstone.webapp.services.account.administration.exceptions.single.LockedException;
import cornerstone.webapp.services.account.administration.exceptions.single.NoAccountException;
import cornerstone.webapp.services.account.administration.exceptions.single.UnverifiedEmailException;
import cornerstone.webapp.services.jwt.JWTService;
import cornerstone.webapp.services.rsa.store.local.SigningKeySetupException;
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

    private final AccountManager accountAdmin;
    private final JWTService JWTService;

    @Inject
    public LoginRestService(final AccountManager accountAdmin, final JWTService JWTService) {
        this.accountAdmin = accountAdmin;
        this.JWTService = JWTService;
    }

    @POST
    public Response authenticateUser(final AccountEmailPassword accountEmailPassword) throws
            UnverifiedEmailException,
            LockedException,
            NoAccountException, SigningKeySetupException {

        if (null != accountEmailPassword &&
            null != accountEmailPassword.getEmail() &&
            null != accountEmailPassword.getPassword()) {

            final boolean authenticated;
            authenticated = accountAdmin.login(accountEmailPassword.getEmail(), accountEmailPassword.getPassword());

            if ( authenticated ) {
                final String jwt = JWTService.createJws(accountEmailPassword.getPassword(), null);
                logger.info(String.format(
                        AlignedLogMessages.FORMAT__OFFSET_35C_C_STR,
                        AlignedLogMessages.OFFSETS_ALIGNED_CLASSES.get(getClass().getName()),
                        "TOKEN GRANTED", accountEmailPassword.getEmail())
                );

                return Response.status(Response.Status.ACCEPTED).entity(jwt).build();

            } else {
                logger.info(String.format(
                        AlignedLogMessages.FORMAT__OFFSET_35C_C_STR,
                        AlignedLogMessages.OFFSETS_ALIGNED_CLASSES.get(getClass().getName()),
                        "TOKEN DENIED", accountEmailPassword.getEmail())
                );
            }
        }

        return Response.status(Response.Status.FORBIDDEN).entity(new SingleErrorResponse(Response.Status.FORBIDDEN.getStatusCode(), "Access denied.")).build();
    }
}
