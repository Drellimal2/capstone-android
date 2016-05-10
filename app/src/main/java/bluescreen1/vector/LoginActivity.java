package bluescreen1.vector;

import android.content.Intent;
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

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Dane on 3/15/2016.
 */
public class LoginActivity extends AppCompatActivity{

    EditText email_username_et;
    EditText password_et;
    String user;
    String jsonuser = "0";
    Intent main;
    Intent reg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        Button login = (Button) findViewById(R.id.login_login_button);
        Button signup = (Button) findViewById(R.id.login_signup_button);
        email_username_et = (EditText) findViewById(R.id.login_username_email);
        password_et = (EditText) findViewById(R.id.login_password);

        main = new Intent( this, MainActivity.class);
        reg = new Intent( this, SignUpActivity.class);
        RadioGroup user_rg = (RadioGroup) findViewById(R.id.login_user_radio_group);
        user_rg.check(R.id.login_player_radio);
        user = getChecked(user_rg);
        user_rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                user = getChecked(group);
                toastit(user);
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

    private String getChecked(RadioGroup rg) {
        int rid = rg.getCheckedRadioButtonId();
        switch(rid){
            case R.id.login_creator_radio:
                return "Creator";
            case R.id.login_player_radio:
                return "Player";
            default:
                return "Player";
        }
    }

    private boolean login(String email, String password) {
//        if(email=="user" && password == "pass") {
//            return true;
//        } else {
//            return false;
//        }
        loginUser(email, password);
        while (jsonuser.equals("0")) {
            if (!jsonuser.equals("-1")) {
                return true;
            } else{
                return false;
            }
        }
        jsonuser = "0";
        return email.equals("user") && password.equals("pass");

    }


    private void toastit(String text){
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();

    }

    private void loginUser(final String email, final String password){

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Config.LOGIN_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(LoginActivity.this,response,Toast.LENGTH_LONG).show();
                        jsonuser = response;
                        startActivity(main);
                        finish();

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(LoginActivity.this,error.toString(),Toast.LENGTH_LONG).show();
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
