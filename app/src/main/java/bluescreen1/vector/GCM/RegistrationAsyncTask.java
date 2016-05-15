package bluescreen1.vector.GCM;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.util.HashMap;
import java.util.Map;

import bluescreen1.vector.Config;
import bluescreen1.vector.Game.GameDB;
import bluescreen1.vector.MainActivity;
import bluescreen1.vector.Models.UserEntry;
import bluescreen1.vector.R;
import bluescreen1.vector.VectorApplication;

/**
 * Created by Dane on 5/4/2016.
 */
public class RegistrationAsyncTask extends AsyncTask<String, Void, Void> {

    Activity mActivity;
    ProgressDialog mProgressDialog;
    String token;

    public RegistrationAsyncTask(Activity activity){
        mActivity = activity;
        if (activity != null){
            mProgressDialog = new ProgressDialog(activity);
            mProgressDialog.setMessage("Registering...");
            mProgressDialog.setCancelable(false);
        }

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
//        if (mProgressDialog != null) mProgressDialog.show();
    }

    @Override
    protected Void doInBackground(String... params) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mActivity);
        //String senderId = params[0];
        //Log.d("test", "sender id : " + senderId);

        try {
            // Get the token from GCM server.
            InstanceID instanceID = InstanceID.getInstance(mActivity);
            token = instanceID.getToken(mActivity.getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

            Log.i("test", "GCM Registration Token: " + token);

            sendTokenToServer();

            // Storing that the token has already been sent.
            sharedPreferences.edit().putBoolean(Config.KEY_TOKEN_SENT_TO_SEVER, true).apply();

        } catch (Exception e) {
            Log.d("test", "Failed to complete token refresh", e);
            // If an exception happens while fetching the new token or updating our registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
            sharedPreferences.edit().putBoolean(Config.KEY_TOKEN_SENT_TO_SEVER, false).apply();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        //if (mProgressDialog != null) mProgressDialog.dismiss();
       // ((MainActivity)mActivity).updateMessage("Device Token : " + token);
    }


    private void sendTokenToServer(){

        String appServerTokenUrl = String.format("%sregister_token", Config.REGISTER_URL);
        Log.d("GCM TOKEN TO SERVER", appServerTokenUrl);
        StringRequest request = new StringRequest(Request.Method.POST, appServerTokenUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("TOKEN REGISTER", response);

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("TOKEN ERROR" , error.toString());
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<>();
                params.put("token", token);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put("Authorization", "Token token="+currentUserToken());
                return params;
            }
        };

        VectorApplication.getInstance().addToRequestQueue(request);
    }

    private String currentUserToken(){
            String[] userTokenColumn = {UserEntry.COLUMN_NAME_TOKEN};
            final GameDB gameDB = new GameDB(mActivity.getApplication());
            SQLiteDatabase db = gameDB.getWritableDatabase();
            String sortOrder =
                    UserEntry.COLUMN_NAME_USER_ID + " DESC";

            Cursor c = db.query(
                    UserEntry.TABLE_NAME,  // The table to query
                    userTokenColumn,                               // The columns to return
                    null,                                // The columns for the WHERE clause
                    null,                            // The values for the WHERE clause
                    null,                                     // don't group the rows
                    null,                                     // don't filter by row groups
                    sortOrder                                 // The sort order
            );

            c.moveToFirst();
            return c.getString(0);
    }


}