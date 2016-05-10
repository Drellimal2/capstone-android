package bluescreen1.vector;

import com.google.android.gms.iid.InstanceIDListenerService;

/**
 * Created by Dane on 5/4/2016.
 */
public class MyInstanceIDListenerService extends InstanceIDListenerService {

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        // Fetch updated token from the server again and send it to application server.
        new RegistrationAsyncTask(null).execute();
    }
}