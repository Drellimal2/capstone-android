package bluescreen1.vector.Game;

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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import bluescreen1.vector.Config;
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
    JSONObject jgame;

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
        TextView status = (TextView) findViewById(R.id.game_details_status);
        Button button = (Button) findViewById(R.id.game_details_button);
        final TextView countdown = (TextView) findViewById(R.id.game_details_countdown);
        TextView desc = (TextView) findViewById(R.id.game_details_desc);
        Button play = (Button) findViewById(R.id.game_details_play);
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
}
