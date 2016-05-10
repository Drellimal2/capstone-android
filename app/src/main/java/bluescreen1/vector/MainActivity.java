package bluescreen1.vector;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        String user = getIntent().getExtras().getString("user");
        toastit(user);
    }

    public void updateMessage(String deviceToken){
        Toast.makeText(this, deviceToken, Toast.LENGTH_LONG).show();
    }

    private void toastit(String text){
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();

    }

}
