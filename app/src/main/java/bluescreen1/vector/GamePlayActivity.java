package bluescreen1.vector;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import bluescreen1.vector.Game.GameDB;
import bluescreen1.vector.Models.UserEntry;

import static java.lang.Math.toRadians;


/**
 * Created by Dane on 5/11/2016.
 */
public class GamePlayActivity extends AppCompatActivity implements
    GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, ClueSolverDialog.NoticeDialogListener {
    private boolean isReceiverRegistered;
    private BroadcastReceiver broadcastReceiver;
    int userid;
    String token;
    String ptype;
    JSONObject closest_clue;
    String game;
    double closestClue =100000000;
    String gtitle;
    JSONObject jgame;
    int game_id;
    ArrayList<JSONObject> clues;

    protected Location mLastLocation;
    protected LocationRequest mLocationRequest;
    TextView title, countdown;

    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 2000;

    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    protected GoogleApiClient mGoogleApiClient;


    private void getData(){

        String url = Config.GAME_URL + game_id + "/clues/";


        final Context context= this;
        JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(url,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray jsonObject) {
                        JSONArray ja = null;
                        ArrayList<JSONObject> jobj = new ArrayList<>();
                        try {
                            ja = jsonObject;
                            for( int x = 0; x < ja.length(); x++){
                                jobj.add(ja.getJSONObject(x));
                            }

                            clues= jobj;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("Error: ", error.getMessage());
            }
        });

        VectorApplication vapp = VectorApplication.getInstance();
        vapp.addToRequestQueue(jsonObjectRequest);
    }

    private void check_clues(Location location){
        LinearLayout indicator = (LinearLayout) findViewById(R.id.gameplay_indicator);
        for(JSONObject clue: clues){
            double clueDist = hav(location, clue );
            if(clueDist <= closestClue){
                closestClue = clueDist;
                closest_clue = clue;
            }
            if ( clueDist < 6.0){

                try {
                    Log.i("CLUE", "" +clue.getInt("id"));


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        if(closestClue > 50){
            indicator.setBackgroundColor(getResources().getColor(R.color.ice_cold));
        }

        if(closestClue > 40){
            indicator.setBackgroundColor(getResources().getColor(R.color.cold));
        }

        if(closestClue > 30){
            indicator.setBackgroundColor(getResources().getColor(R.color.cool));
        }
        if(closestClue > 20){
            indicator.setBackgroundColor(getResources().getColor(R.color.warm));
        }
        if(closestClue > 10){
            indicator.setBackgroundColor(getResources().getColor(R.color.hot));
        }
        if(closestClue > 50){
            indicator.setBackgroundColor(getResources().getColor(R.color.sun_hot));
        }
        confirmFireMissiles(closest_clue);
    }

    private void toastit(String text){
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();

    }

    protected double hav(Location location, JSONObject clue){


        double lat = location.getLatitude();
        double lon = location.getLongitude();
        double cluelat = 0;
        double cluelon = 0;
        try {
            cluelat = clue.getDouble("latitude");
            cluelon = clue.getDouble("longitude");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        long R = 6371000; // metres
        double l1 = toRadians(lat);
        double l2 = toRadians(cluelat);
        double dlat = l1-l2;
        double dlon = toRadians(lon-cluelon);

        double a = Math.sin(dlat/2) * Math.sin(dlat/2) +
                Math.cos(l1) * Math.cos(l2) *
                        Math.sin(dlon/2) * Math.sin(dlon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        double d = R * c;
        return d;
    }

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
        game = getIntent().getStringExtra("GAME");
        try {
            jgame = new JSONObject(game);
            game_id = jgame.getInt("id");
            gtitle = jgame.getString("name");
        }catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
//        getData(userid);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gameplay);
        setData();
        title = (TextView) findViewById(R.id.gameplay_title);
        countdown = (TextView) findViewById(R.id.gameplay_countdown);
        title.setText(gtitle);
        getData();
        buildGoogleApiClient();
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                countdown.setText("hi");
            }
        };
        registerReceiver();


    }

    protected synchronized void buildGoogleApiClient() {
        Log.i("GAPI", "Building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();


        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onConnected(Bundle bundle) {
//        toastit("STARTED");
        startLocationUpdates();

    }

    protected void startLocationUpdates() {


        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
//        countdown.setText(location.getLongitude() + ", " + location.getLatitude());
//        toastit(location.getLongitude() + ", " + location.getLatitude());

        check_clues(location);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();

        super.onStop();
    }

    public void confirmFireMissiles(JSONObject clue) {
        DialogFragment newFragment = ClueSolverDialog.newInstance(clue);
        newFragment.show(getSupportFragmentManager(), "missiles");
        stopLocationUpdates();
    }

    private void registerReceiver(){
        if(!isReceiverRegistered) {
            LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,
                    new IntentFilter("ho"));
            isReceiverRegistered = true;
        }
    }

    protected void discover_clue(int clue_id){
        String url = Config.GAME_URL + game_id + "/clues/" + clue_id +"/discover/";

        StringRequest stringRequest = new StringRequest(Request.Method.PUT, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
//                toastit(s);
                toastit("Congrats you have claimed this clue;");
                startLocationUpdates();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                volleyError.printStackTrace();
                startLocationUpdates();
            }
        }){
            @Override
            public Map<String, String> getHeaders(){
                Map<String, String> params = new HashMap<String, String>();
                params.put("Authorization", "Token token="+token);
                return params;
            }
        };

        VectorApplication vapp = VectorApplication.getInstance();
        vapp.addToRequestQueue(stringRequest);
    }



    @Override
    public void onDialogPositiveClick(DialogFragment dialog, boolean b, int cid) {
        if (b){
//            toastit("Congrats you have claimed this clue;" + cid);
            discover_clue(cid);
        } else {
            toastit("Sorry that answer is incorrect");
//            dialog.show(getSupportFragmentManager(), "retry");
//            startLocationUpdates();
        }
//            startLocationUpdates();

    }


    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        startLocationUpdates();
    }
}
