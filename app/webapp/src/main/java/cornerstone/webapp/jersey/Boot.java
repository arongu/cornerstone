package cornerstone.webapp.jersey;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;

import javax.inject.Inject;

/**
 * - entry point -> web.xml
 * - you can manually register classes, but then you need to add annotations like @Singleton in the registered class file,
 *   or explicitly bind them with the binder class
 * - JerseyBinder instance created, and registered, which provides injection for classes
 * - scans packages with annotations (@Path, @Provides)
 *
 * https://stackoverflow.com/questions/18914130/when-to-use-singleton-annotation-of-jersey
 * https://stackoverflow.com/questions/45625925/what-exactly-is-the-resourceconfig-class-in-jersey-2
*/
public class Boot extends ResourceConfig {
    @Inject
    public Boot(final ServiceLocator serviceLocator) {
        ServiceLocatorUtilities.enableImmediateScope(serviceLocator);
        packages("cornerstone.webapp");
        register(RolesAllowedDynamicFeature.class); // this required to the roles annotation to work
        register(new ApplicationBinder());          // register ApplicationBinder
    }
}