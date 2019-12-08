package cornerstone.workflow.app.jersey;

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
public class JerseyApplication extends ResourceConfig {
    public JerseyApplication() {
        register(new JerseyBinder());
        packages("cornerstone.workflow.app");
    }
}
