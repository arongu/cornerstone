package cornerstone.webapp.rest.endpoint.account;

import cornerstone.webapp.common.DefaultLogMessages;
import cornerstone.webapp.rest.exceptions.BadRequestException;
import cornerstone.webapp.services.account.administration.AccountAdminException;
import cornerstone.webapp.services.account.administration.AccountAdminInterface;
import cornerstone.webapp.services.account.administration.AccountAdminMultipleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.util.List;

@Provider
@Singleton
@Path("/account")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AccountRestService {
    private static final Logger logger = LoggerFactory.getLogger(AccountRestService.class);
    private final AccountAdminInterface accountAdmin;

    @Inject
    public AccountRestService(final AccountAdminInterface accountAdmin) {
        this.accountAdmin = accountAdmin;
        logger.info(String.format(DefaultLogMessages.MESSAGE_CONSTRUCTOR_CALLED, getClass().getName()));
    }

    @POST
    public Response create(final EmailAndPassword account) throws AccountAdminException, BadRequestException {
        if (null != account) {
            accountAdmin.create(account.email, account.password, false, false);
            return Response.status(Response.Status.CREATED).entity(account.email).build();

        } else {
            throw new BadRequestException();
        }
    }

    @DELETE
    public Response delete(final String emailAddress) throws AccountAdminException, BadRequestException {
        if (null != emailAddress) {
            accountAdmin.delete(emailAddress);
            return Response.status(Response.Status.OK).entity(emailAddress).build();

        } else {
            throw new BadRequestException();
        }
    }

    // /mass
    @POST
    @Path("/mass")
    public Response massCreate(final List<EmailAndPassword> accounts) throws AccountAdminMultipleException, BadRequestException {
        if (accounts != null && !accounts.isEmpty()) {
            accountAdmin.create(accounts);
            return Response.status(Response.Status.CREATED).entity(accounts).build();

        } else {
            throw new BadRequestException();
        }
    }

    @DELETE
    @Path("/mass")
    public Response massDelete(final List<String> emailAddresses) throws AccountAdminMultipleException, BadRequestException {
        if (null != emailAddresses) {
            accountAdmin.delete(emailAddresses);
            return Response.status(Response.Status.OK).entity(emailAddresses).build();

        } else {
            throw new BadRequestException();
        }
    }
}
