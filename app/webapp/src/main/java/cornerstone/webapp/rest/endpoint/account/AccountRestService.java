package cornerstone.webapp.rest.endpoint.account;

import cornerstone.webapp.common.DefaultLogMessages;
import cornerstone.webapp.rest.exceptions.BadRequestException;
import cornerstone.webapp.services.account.administration.AccountManagerException;
import cornerstone.webapp.services.account.administration.AccountManager;
import cornerstone.webapp.services.account.administration.AccountManagerMultipleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Singleton
@Path("/account")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AccountRestService {
    private static final Logger logger = LoggerFactory.getLogger(AccountRestService.class);
    private final AccountManager accountManager;

    @Inject
    public AccountRestService(final AccountManager accountManager) {
        this.accountManager = accountManager;
        logger.info(String.format(DefaultLogMessages.MESSAGE_CONSTRUCTOR_CALLED, getClass().getName()));
    }

    @POST
    public Response create(final AccountEmailPassword account) throws AccountManagerException, BadRequestException {
        if (null != account) {
            accountManager.create(account.email, account.password, false, false);
            return Response.status(Response.Status.CREATED).entity(account.email).build();

        } else {
            throw new BadRequestException();
        }
    }

    @DELETE
    public Response delete(final String emailAddress) throws AccountManagerException, BadRequestException {
        if (null != emailAddress) {
            accountManager.delete(emailAddress);
            return Response.status(Response.Status.OK).entity(emailAddress).build();

        } else {
            throw new BadRequestException();
        }
    }

    // /mass
    @POST
    @Path("/mass")
    public Response massCreate(final List<AccountEmailPassword> accounts) throws AccountManagerMultipleException, BadRequestException {
        if (accounts != null && !accounts.isEmpty()) {
            accountManager.create(accounts);
            return Response.status(Response.Status.CREATED).entity(accounts).build();

        } else {
            throw new BadRequestException();
        }
    }

    @DELETE
    @Path("/mass")
    public Response massDelete(final List<String> emailAddresses) throws AccountManagerMultipleException, BadRequestException {
        if (null != emailAddresses) {
            accountManager.delete(emailAddresses);
            return Response.status(Response.Status.OK).entity(emailAddresses).build();

        } else {
            throw new BadRequestException();
        }
    }
}
