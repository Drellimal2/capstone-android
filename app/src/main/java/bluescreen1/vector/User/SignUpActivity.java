package bluescreen1.vector.User;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;

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
 * Created by Dane on 5/6/2016.
 */
public class SignUpActivity extends AppCompatActivity {

    EditText email_username_et;
    EditText password_et;
    EditText confirm_pass_et;
    EditText name;
    String user;
    String jsonuser = "0";
    final GameDB gameDB = new GameDB(this);
    Intent main;
    SQLiteDatabase db;

    Intent login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = gameDB.getWritableDatabase();
        setContentView(R.layout.activity_sign_up);
        Button cancel = (Button) findViewById(R.id.signup_cancel_button);
        final Button signup = (Button) findViewById(R.id.signup_signup_button);
        email_username_et = (EditText) findViewById(R.id.signup_username_email);
        password_et = (EditText) findViewById(R.id.signup_password);
        confirm_pass_et = (EditText) findViewById(R.id.sign_up_confirm_password);
        name = (EditText) findViewById(R.id.signup_name);
        Intent i = getIntent();
        if( i.hasExtra("username")){
            email_username_et.setText(i.getStringExtra("username"));
        }
        if( i.hasExtra("pass")){
            password_et.setText(i.getStringExtra("pass"));
        }



        main = new Intent( this, MainActivity.class);
        login =  new Intent(this, LoginActivity.class);

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO Authenticate
                //TODO Save user to local db.
                String email = email_username_et.getText().toString();
                String password = password_et.getText().toString();
                main.putExtra("user", user);
                signup();
//                if(login(email, password)){
//                } else {
//                    toastit("Could not be logged in.");
//                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO Authenticate
                //TODO Save user to local db.
                startActivity(login);
                finish();
            }
        });
    }


    private void signup() {
        String n = name.getText().toString();
        String uemail = email_username_et.getText().toString();
        String passw = password_et.getText().toString();
        String cpassw = confirm_pass_et.getText().toString();
        String fname= "", lname="";

        int errors = 0;
        if(!passw.equals(cpassw)) {
            toastit("Passwords must match");
            errors += 1;
        }
        if(n.trim().equals("")){
            toastit("Name cannot be left blank.");
            errors +=1;
        } else {
            String[] names = n.split(" ");
            fname = names[0];
            if(names.length == 1){
                lname = " ";
            } else {
                lname= names[names.length-1];
            }
        }

        if (errors ==0){
            signinUser(uemail, passw, fname,lname);
        }
    }


    private void toastit(String text){
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();

    }

    private void signinUser(final String email, final String password, final String fname, final String lname){
        JSONObject mparams;
        Map<String,String> params = new HashMap<String, String>();
        Map<String,JSONObject> jparams = new HashMap<String, JSONObject>();

        JSONObject pparams;
        params.put("email",email);
        params.put("password",password);
        params.put("firstname", fname);
        params.put("lastname", lname);
        pparams = new JSONObject(params);
        jparams.put("user", pparams);
        mparams = new JSONObject(jparams);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Config.REGISTER_URL, mparams,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        savetodb(response);
                        Toast.makeText(SignUpActivity.this,response.toString(),Toast.LENGTH_LONG).show();
                        startActivity(main);
                        // TODO attach token to register;
                        finish();
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

    private boolean savetodb(JSONObject juser) {

        ContentValues values = new ContentValues();
        try {
            values.put(UserEntry.COLUMN_NAME_USER_ID, juser.getInt("id") );
            values.put(UserEntry.COLUMN_NAME_TOKEN, juser.getString("auth_token") );
            values.put(UserEntry.COLUMN_NAME_FIRSTNAME, juser.getString("firstname") );
            values.put(UserEntry.COLUMN_NAME_LASTNAME, juser.getString("lastname") );
            user = ""+1;
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

}
