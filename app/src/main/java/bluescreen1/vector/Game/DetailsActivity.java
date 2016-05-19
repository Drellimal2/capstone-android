package bluescreen1.vector.Game;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import bluescreen1.vector.Config;
import bluescreen1.vector.GCM.RegistrationAsyncTask;
import bluescreen1.vector.GamePlayActivity;
import bluescreen1.vector.Models.UserEntry;
import bluescreen1.vector.R;
import bluescreen1.vector.VectorApplication;

/**
 * Created by Dane on 5/11/2016.
 */
public class DetailsActivity extends AppCompatActivity {

    int userid;
    String token;
    String sgame;
    String ptype;
    TextView status;
    Button play, button;
    JSONObject jgame;
    Activity thisActivity = this;

    protected void setData(){
        final GameDB gameDB = new GameDB(this);
        SQLiteDatabase db = gameDB.getWritableDatabase();
        String sortOrder =
                UserEntry.COLUMN_NAME_USER_ID + " DESC";

        Cursor c = db.query(
                UserEntry.TABLE_NAME,  // The table to query
                UserEntry.COLUMNS,                               // The columns to return
                null,                                // The columns for the WHERE clause
                null,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        c.moveToFirst();
        userid = c.getInt(0);
        token = c.getString(1);
        ptype = c.getString(4);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.details_game);
        setData();
        Intent callingIntent = getIntent();
        sgame = callingIntent.getStringExtra("game");
        TextView title = (TextView) findViewById(R.id.game_details_title);
        TextView start_time = (TextView) findViewById(R.id.game_details_start_time);
        TextView end_time = (TextView) findViewById(R.id.game_details_end_time);
        status = (TextView) findViewById(R.id.game_details_status);

        button = (Button) findViewById(R.id.game_details_button);
        final TextView countdown = (TextView) findViewById(R.id.game_details_countdown);
        TextView desc = (TextView) findViewById(R.id.game_details_desc);
        play = (Button) findViewById(R.id.game_details_play);
        final Intent playIntent = new Intent(this, GamePlayActivity.class);
        playIntent.putExtra("GAME", sgame);
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(playIntent);
            }
        });

        if (callingIntent.hasExtra("game")){
            try {

                JSONObject game = new JSONObject(sgame);
                jgame = game;
                title.setText(game.getString("name"));
                final String start_string = game.getString("start_time");
                start_time.setText(get_date(start_string));
                final String end_string = game.getString("end_time");
                end_time.setText(get_date(end_string));
                long time = new Date().getTime();
                String sstatus = getstatus(start_string, end_string);
                status.setText(sstatus);
                Date d = new Date();
                String ending = "";
                if(sstatus.equals("NOT STARTED")){
                    d = get_date_d(start_string);

                    ending = " till start";
                } else if (sstatus.equals("RUNNING")){
                    d = get_date_d(end_string);
                    ending = " till end";
                } else {
                    countdown.setVisibility(View.GONE);
                }
                long dif = d.getTime() - time;
                final String finalEnding = ending;
                new CountDownTimer(dif, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        long secs = millisUntilFinished/1000;
                        long mins = secs/60;
                        secs %= 60;
                        long hours = mins/60;
                        mins %= 60;
                        countdown.setText(String.format(" %02d : %02d : %02d ", hours, mins, secs)+ finalEnding);

                    }

                    @Override
                    public void onFinish() {
                        countdown.setText("Its Time");
                        getstatus(start_string, end_string);
                    }
                }.start();

                desc.setText(game.getString("description"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if(callingIntent.getIntExtra("in", 1) == 1){
            button.setText("Leave Game");
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        leave(jgame.getInt("id"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

        } else {
            button.setText("Join Game");
            play.setVisibility(View.GONE);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    try {
                        join(jgame.getInt("id"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

    }

    private void leave(int game_id){
        String url = Config.GAME_URL + game_id + "/quit";
        final Intent intent = new Intent(this,DetailsActivity.class);

        StringRequest jsonObjectRequest = new StringRequest(Request.Method.DELETE,url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String ja) {

                        intent.putExtra("game", sgame);
                        intent.putExtra("in",0);
                        startActivity(intent);
                        finish();

                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("Error: ", error.getMessage());
                error.printStackTrace();
            }
        }){
            @Override
            public Map<String, String> getHeaders(){
                Map<String, String>  params = new HashMap<String, String>();
                params.put("Authorization", "Token token="+token);

                return params;
            }
        };

            VectorApplication vapp = VectorApplication.getInstance();
            vapp.addToRequestQueue(jsonObjectRequest);
        }

    private void join(int game_id){

        String url = Config.GAME_URL + game_id + "/join";
        final Intent intent = new Intent(this,DetailsActivity.class);

        StringRequest jsonObjectRequest = new StringRequest(Request.Method.PUT,url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String ja) {
                        Log.i("STUDD",ja);
                        intent.putExtra("game", sgame);
                        intent.putExtra("in",1);
                        new RegistrationAsyncTask(thisActivity).execute();//Send GCM token to the server when you join a game
                        startActivity(intent);
                        finish();


                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("Error: ", error.getMessage());
                error.printStackTrace();
            }
        }){
            @Override
            public Map<String, String> getHeaders(){
                Map<String, String>  params = new HashMap<>();
                params.put("Authorization", "Token token="+token);

                return params;
            }
        };

        VectorApplication vapp = VectorApplication.getInstance();
        vapp.addToRequestQueue(jsonObjectRequest);
    }

    protected String getstatus(String start_string, String end_string){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault());
        String[] start_datetime = start_string.split("T");
        String[] end_datetime = end_string.split("T");
        String start_text = start_datetime[0] + " " + start_datetime[1].substring(0, start_datetime[1].length()-5);
        String end_text = end_datetime[0] + " " + end_datetime[1].substring(0, end_datetime[1].length()-5);
        //            Date start = dateFormat.parse(start_text);
        Date start= get_date_d(start_string);
        Date end = get_date_d(end_string);
//            Date end = dateFormat.parse(end_text);
        Date now = new Date();
        if(start.after(now)){
            status.setTextColor(getResources().getColor(R.color.red));
                play.setVisibility(View.GONE);
            return "NOT STARTED";
        } else {
            if(end.after(now)){
                status.setTextColor(getResources().getColor(R.color.grassgreen));

                return "RUNNING";
            } else {
                status.setTextColor(getResources().getColor(R.color.sand));
                play.setVisibility(View.GONE);
                button.setVisibility(View.GONE);
                play.setEnabled(false);
                return "ENDED";
            }
        }
//        return "ACTIVE";

    }

    protected String get_date(String date){


        String[] date_datetime = date.split("T");
        String date_text = date_datetime[0] + " " +
                date_datetime[1].substring(0, date_datetime[1].length()-5);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault());
        Date start = new Date();
        try {
            start = dateFormat.parse(date_text);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        DateFormat df = DateFormat.getDateTimeInstance();

        calendar.setTime(start);
        calendar.add(Calendar.HOUR_OF_DAY, -5);
        return df.format(calendar.getTime());
    }

    protected Date get_date_d(String date){

        String[] date_datetime = date.split("T");
        String date_text = date_datetime[0] + " " +
                date_datetime[1].substring(0, date_datetime[1].length()-5);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault());
        Date start = new Date();
        try {
            start = dateFormat.parse(date_text);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        DateFormat df = DateFormat.getDateTimeInstance();

        calendar.setTime(start);
        calendar.add(Calendar.HOUR_OF_DAY, -5);
        return calendar.getTime();
    }
}
