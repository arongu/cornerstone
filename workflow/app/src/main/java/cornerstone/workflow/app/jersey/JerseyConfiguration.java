package cornerstone.workflow.app.jersey;

import cornerstone.workflow.app.rest.account.AccountServiceBulkExceptionMapper;
import cornerstone.workflow.app.rest.account.AccountServiceExceptionMapper;
import cornerstone.workflow.app.rest.rest_exceptions.BadRequestExceptionMapper;
import cornerstone.workflow.app.services.account_service.AccountServiceBulkException;
import org.glassfish.jersey.server.ResourceConfig;

/*
   - entry point -> web.xml
   - you can manually register classes, but then you need to add annotations like @Singleton in the registered class file,
     or explicitly bind them with the binder class
   - JerseyBinder instance created, and registered, which provides injection for classes
   - scans packages with annotations (@Path, @Provides)

   https://stackoverflow.com/questions/18914130/when-to-use-singleton-annotation-of-jersey
   https://stackoverflow.com/questions/45625925/what-exactly-is-the-resourceconfig-class-in-jersey-2

*/
public class JerseyConfiguration extends ResourceConfig {
    public JerseyConfiguration() {
        register(new JerseyBinder());
        packages("cornerstone.workflow.app.datasource");
        packages("cornerstone.workflow.app.services");
        packages("cornerstone.workflow.app.rest");
    }
}
