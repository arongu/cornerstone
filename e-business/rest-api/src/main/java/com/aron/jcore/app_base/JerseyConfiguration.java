package com.aron.jcore.app_base;

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
        packages("com.aron.jcore");
    }
}
