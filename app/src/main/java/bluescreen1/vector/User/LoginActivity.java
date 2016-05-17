package bluescreen1.vector.User;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import bluescreen1.vector.Config;
import bluescreen1.vector.Game.GameDB;
import bluescreen1.vector.MainActivity;
import bluescreen1.vector.Models.UserEntry;
import bluescreen1.vector.R;
import bluescreen1.vector.VectorApplication;

/**
 * Created by Dane on 3/15/2016.
 */
public class LoginActivity extends AppCompatActivity{
    final GameDB gameDB = new GameDB(this);
    EditText email_username_et;
    EditText password_et;
    int user;
    String jsonuser = "0";
    Intent main;
    Intent reg;
    SQLiteDatabase db;
    RadioGroup user_rg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        db = gameDB.getWritableDatabase();

        main = new Intent( this, MainActivity.class);
        String[] projection = {
                UserEntry.COLUMN_NAME_USER_ID
        };


        String sortOrder =
                UserEntry.COLUMN_NAME_USER_ID + " DESC";

        Cursor c = db.query(
                UserEntry.TABLE_NAME,  // The table to query
                projection,                               // The columns to return
                null,                                // The columns for the WHERE clause
                null,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        if(c.getCount() == 1){
            startActivity(main);
            finish();
        }
        Button login = (Button) findViewById(R.id.login_login_button);
        Button signup = (Button) findViewById(R.id.login_signup_button);
        email_username_et = (EditText) findViewById(R.id.login_username_email);
        password_et = (EditText) findViewById(R.id.login_password);


        reg = new Intent( this, SignUpActivity.class);
        user_rg = (RadioGroup) findViewById(R.id.login_user_radio_group);
        user_rg.check(R.id.login_player_radio);
        user=1;
        user_rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                user = getChecked(group);
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO Authenticate
                //TODO Save user to local db.
                String email = email_username_et.getText().toString();
                String password = password_et.getText().toString();
                main.putExtra("user", user);
                loginUser(email, password);
//                if(login(email, password)){
//                } else {
//                    toastit("Could not be logged in.");
//                }
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO Authenticate
                //TODO Save user to local db.
                String email = email_username_et.getText().toString();
                String password = password_et.getText().toString();
                if(!email.equals("")){
                    reg.putExtra("username", email);
                }
                if(!password.equals("")){
                    reg.putExtra("pass", password);
                }
                startActivity(reg);
                finish();
            }
        });
    }

    private int getChecked(RadioGroup rg) {
        int rid = rg.getCheckedRadioButtonId();
        switch(rid){
            case R.id.login_creator_radio:
                main.putExtra("ptype",2);
                user=2;
                return 2;
            case R.id.login_player_radio:
                main.putExtra("ptype",1);
                user=1;
                return 1;
            default:
                return 1;
        }
    }

    private boolean savetodb(JSONObject juser) {

        ContentValues values = new ContentValues();
        try {
            values.put(UserEntry.COLUMN_NAME_USER_ID, juser.getInt("id") );
            values.put(UserEntry.COLUMN_NAME_TOKEN, juser.getString("auth_token") );
            values.put(UserEntry.COLUMN_NAME_FIRSTNAME, juser.getString("firstname") );
            values.put(UserEntry.COLUMN_NAME_LASTNAME, juser.getString("lastname") );
            user = getChecked(user_rg);
            values.put(UserEntry.COLUMN_NAME_TYPE, ""+user);

        } catch (JSONException e) {
            e.printStackTrace();
        }

// Insert the new row, returning the primary key value of the new row
        long newRowId;
        newRowId = db.insert(
                UserEntry.TABLE_NAME,
                null,
                values);
        return true;

    }


    private void toastit(String text){
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();

    }

    private void loginUser(final String email, final String password){
        user = getChecked(user_rg);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Config.LOGIN_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        jsonuser = response;
                        try {
                            JSONObject j = new JSONObject(jsonuser);
                            main.putExtra("userid", j.getInt("id"));
//                            Toast.makeText(LoginActivity.this,response,Toast.LENGTH_LONG).show();
                            savetodb(j);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        startActivity(main);
                        finish();

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(LoginActivity.this,"OOPS Something went wrong check your credentials or internet connection.",Toast.LENGTH_LONG).show();
                        error.printStackTrace();
//                        Toast.makeText(Lo)
                        jsonuser = "-1";
                    }
                }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("email",email);
                params.put("password",password);
                return params;
            }

        };

        VectorApplication vapp = VectorApplication.getInstance();
        vapp.addToRequestQueue(stringRequest);
    }

}
