package bluescreen1.vector;

/**
 * Created by Dane on 3/15/2016.
 */

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import bluescreen1.vector.User.LoginActivity;

public class SplashScreen extends Activity {
    private Intent mainMenuIntent;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);
        mainMenuIntent = new Intent(this, LoginActivity.class);
        Thread timer = new Thread() {
            public void run() {
                try {
                    sleep(3000);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
                finally {
                    startActivity(mainMenuIntent);
                }
            }
        };
        timer.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}

