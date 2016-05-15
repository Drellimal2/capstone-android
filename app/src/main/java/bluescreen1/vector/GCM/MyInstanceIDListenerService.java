package bluescreen1.vector.GCM;

import com.google.android.gms.iid.InstanceIDListenerService;

/**
 * Created by Dane on 5/4/2016.
 */
public class MyInstanceIDListenerService extends InstanceIDListenerService {

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();

        new RegistrationAsyncTask(null).execute();
    }
}