package bluescreen1.vector;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Dane on 5/11/2016.
 */
public class DetailsActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.details_game);
        TextView title = (TextView) findViewById(R.id.game_details_title);
        TextView start_time = (TextView) findViewById(R.id.game_details_start_time);
        TextView end_time = (TextView) findViewById(R.id.game_details_end_time);
        TextView status = (TextView) findViewById(R.id.game_details_status);
        final TextView countdown = (TextView) findViewById(R.id.game_details_countdown);
        TextView desc = (TextView) findViewById(R.id.game_details_desc);
        Intent callingIntent = getIntent();
        if (callingIntent.hasExtra("game")){
            try {
                JSONObject game = new JSONObject(callingIntent.getStringExtra("game"));
                title.setText(game.getString("name"));
                String start_string = game.getString("start_time");
                String[] start_datetime = start_string.split("T");
                String start_text = start_datetime[0] + " " +
                    start_datetime[1].substring(0, start_datetime[1].length()-5);
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault());
                Date start = dateFormat.parse(start_text);
                start_time.setText(start.toString());

//                String end_string = game.getString("start_time");
//                String[] end_datetime = start_string.split("T");
//                String end_text = start_datetime[0] + " @ " +
//                        end_datetime[1].substring(0, end_datetime[1].length()-5);
                long time = new Date().getTime();
                long dif = start.getTime() - time;
                new CountDownTimer(dif, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        countdown.setText("" + millisUntilFinished/1000);
                    }

                    @Override
                    public void onFinish() {
                        countdown.setText("Its Time");
                    }
                }.start();
                end_time.setText(game.getString("end_time"));
                desc.setText(game.getString("description"));
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

    }
}
