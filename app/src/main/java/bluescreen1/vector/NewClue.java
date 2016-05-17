package bluescreen1.vector;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import bluescreen1.vector.Game.GameDB;
import bluescreen1.vector.Models.UserEntry;
import bluescreen1.vector.User.LoginActivity;

/**
 * Created by Dane on 5/13/2016.
 */
public class NewClue extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    int userid;
    String token;
    String ptype;
    Location mLastLocation;
    EditText ques, ans, hint, lat, lon;
    Button get_coord;
    private GoogleApiClient googleApiClient;


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
//        toastit(ptype);
    }


    @Override
    protected void onPostResume() {
        super.onPostResume();
        getData(userid);
    }

    protected void logout(){
        final GameDB gameDB = new GameDB(this);
        SQLiteDatabase db = gameDB.getWritableDatabase();

        String selection = UserEntry.COLUMN_NAME_USER_ID + " = ?";
// Specify arguments in placeholder order.
        String[] selectionArgs = { (""+userid) };
// Issue SQL statement.
        db.delete(UserEntry.TABLE_NAME, selection, selectionArgs);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_clue);
        googleApiClient = new GoogleApiClient.Builder(this, this, this).addApi(LocationServices.API).build();

        ques = (EditText) findViewById(R.id.new_clue_question);
        ans = (EditText) findViewById(R.id.new_clue_answer);
        hint = (EditText) findViewById(R.id.new_clue_hint);
        lat = (EditText) findViewById(R.id.new_clue_lat);
        lon = (EditText) findViewById(R.id.new_clue_long);
        get_coord = (Button) findViewById(R.id.new_clue_add_location_button);
//        toastit(""+mLastLocation.getLatitude());

        get_coord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Location a = getLock();
                lat.setText(""+a.getLatitude());
                lon.setText(""+a.getLongitude());
//                toastit(""+mLastLocation.getLatitude());
            }
        });

        Button save = (Button) findViewById(R.id.new_clue_save_button);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validate()){
                    save();
                    finish();
                }
            }
        });

        setData();

    }

    public void updateMessage(String deviceToken){
        Toast.makeText(this, deviceToken, Toast.LENGTH_LONG).show();
    }

    private void toastit(String text){
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();

    }

    private void getData(int userid){

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_games, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_logout){
            Intent login = new Intent(this, LoginActivity.class);
            logout();
            startActivity(login);
            finish();
        }
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (googleApiClient != null) {
            googleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }


    private Boolean validate(){
        boolean good =  true;
        if(ques.getText().toString().trim().equals("")){
            ques.setHint("This is required");
            good =false;
        }
        if(ans.getText().toString().trim().equals("")){
            ans.setHint("This is required");
            good =false;
        }if(hint.getText().toString().trim().equals("")){
            hint.setHint("This is required");
            good =false;
        }if(lat.getText().toString().trim().equals("")){
            lat.setHint("Required");
            good =false;
        }if(lon.getText().toString().trim().equals("")){
            lon.setHint("Required");
            good =false;
        }
        return good;
    }

    private void save() {
        String game = getIntent().getStringExtra("GAME");
        JSONObject jgame = null;
        try {
            jgame = new JSONObject(game);
            int game_id = jgame.getInt("id");
            String url = Config.GAME_URL + game_id + "/clues/";
            JSONObject clue = new JSONObject();
            JSONObject param = new JSONObject();
            try {
                clue.put("question", ques.getText().toString());
                clue.put("answer", ans.getText().toString());
                clue.put("hint", hint.getText().toString());
                clue.put("longitude", Double.parseDouble(lon.getText().toString()));
                clue.put("latitude", Double.parseDouble(lat.getText().toString()));
                param.put("clue", clue);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, param, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject jsonObject) {
                    finish();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {

                }
            }){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("Authorization", "Token token="+token);
                    return params;
                }
            };

            VectorApplication vapp = VectorApplication.getInstance();
            vapp.addToRequestQueue(jsonObjectRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



        @Override
    public void onConnected(Bundle bundle) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                googleApiClient);

    }

    private Location getLock(){
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                googleApiClient);
        return mLastLocation;
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
