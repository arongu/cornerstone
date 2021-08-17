package cornerstone.webapp.services.keys.stores.manager;

import java.util.TimerTask;

public class PublicKeySyncTask extends TimerTask {
    /*
        - get active keys from database
        - drop inactive keys from database (without dropping your own active public key)
        - add your own active key if it is not present
     */

    @Override
    public void run() {

    }
}
