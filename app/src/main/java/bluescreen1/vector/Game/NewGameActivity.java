package bluescreen1.vector.Game;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import bluescreen1.vector.Config;
import bluescreen1.vector.User.LoginActivity;
import bluescreen1.vector.Models.UserEntry;
import bluescreen1.vector.R;
import bluescreen1.vector.VectorApplication;

/**
 * Created by Dane on 5/12/2016.
 */
public class NewGameActivity extends AppCompatActivity {
    SimpleDateFormat sdf;
    int userid;
    String token;
    String ptype;
    EditText startdate;
    EditText enddate;
    EditText desc;
    EditText title;
    Button save;
    Calendar myCalendar;
    Calendar myEndCalendar;

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
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_games, menu);
        return true;
    }


    private void toastit(String text){
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();

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
        }
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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


    private void setEndData() {
        myEndCalendar = Calendar.getInstance();
        final TimePickerDialog.OnTimeSetListener etime = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                myEndCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                myEndCalendar.set(Calendar.MINUTE, minute);
                myEndCalendar.set(Calendar.SECOND,0);
                myEndCalendar.set(Calendar.MILLISECOND,0);
                updateEndTime();
            }
        };

        final TimePickerDialog etimep = new TimePickerDialog(NewGameActivity.this, etime, myEndCalendar
                .get(Calendar.HOUR_OF_DAY), myEndCalendar.get(Calendar.MINUTE), true);
        final DatePickerDialog.OnDateSetListener edate = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myEndCalendar.set(Calendar.YEAR, year);
                myEndCalendar.set(Calendar.MONTH, monthOfYear);
                myEndCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                etimep.show();

            }

        };


        final DatePickerDialog enddialog = new DatePickerDialog(NewGameActivity.this, edate, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH));


        enddialog.show();

    }

    private void updateEndTime(){
        String myFormat = "yyyy-MM-dd HH:mm:ss"; //In which you need put here
        sdf = new SimpleDateFormat(myFormat, Locale.US);
        enddate.setText(sdf.format(myEndCalendar.getTime()));
    }

    private void setStartData() {
        myCalendar = Calendar.getInstance();
        final TimePickerDialog.OnTimeSetListener time = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                myCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                myCalendar.set(Calendar.MINUTE, minute);
                myCalendar.set(Calendar.SECOND,0);
                myCalendar.set(Calendar.MILLISECOND,0);
                updateStartTime();
            }
        };

        final TimePickerDialog timep = new TimePickerDialog(NewGameActivity.this, time, myCalendar
                .get(Calendar.HOUR_OF_DAY), myCalendar.get(Calendar.MINUTE), true);
        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                timep.show();

            }

        };


        final DatePickerDialog startdialog = new DatePickerDialog(NewGameActivity.this, date, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH));


        startdialog.show();

    }

    private void updateStartTime(){
        String myFormat = "yyyy-MM-dd HH:mm:ss"; //In which you need put here
        sdf = new SimpleDateFormat(myFormat, Locale.US);
        startdate.setText(sdf.format(myCalendar.getTime()));
    }

    private boolean validation(){
        boolean res = true;
        if(title.getText().toString().equals("")){
            title.setHint("This is required.");
            res = false;
        }
        if(startdate.getText().toString().equals("")){
            startdate.setHint("This is required.");
            res = false;
        }
        if(enddate.getText().toString().equals("")){
            enddate.setHint("This is required.");
            res = false;
        }
        return res;
    }

    private void makeGame(){

        JSONObject mparams;
        final Map<String,String> params = new HashMap<String, String>();
        Map<String,JSONObject> jparams = new HashMap<String, JSONObject>();

        JSONObject pparams;
        params.put("name",title.getText().toString());
        myCalendar.add(Calendar.HOUR_OF_DAY, 5 );
        myEndCalendar.add(Calendar.HOUR_OF_DAY,5);
        params.put("start_time",sdf.format(myCalendar.getTime()));
        params.put("end_time", sdf.format(myEndCalendar.getTime()));
        params.put("description", desc.getText().toString());
        pparams = new JSONObject(params);
        jparams.put("game", pparams);
        mparams = new JSONObject(jparams);
        Log.e("OH WELL", mparams.toString());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Config.GAME_URL, mparams,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
//                        toastit(response.toString());
                        finish();

                        // TODO attach token to register;
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("Error: ", error.getMessage());
                error.printStackTrace();
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Authorization", "Token token="+token);
                return params;
            }
        };
        Log.i("oh well", "Token token="+token);
        VectorApplication vapp = VectorApplication.getInstance();
        vapp.addToRequestQueue(jsonObjectRequest);



    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_game);
        setData();
        startdate = (EditText)findViewById(R.id.new_game_start_date);
        enddate = (EditText) findViewById(R.id.new_game_end_date);
        desc = (EditText) findViewById(R.id.new_game_desc);
        title = (EditText) findViewById(R.id.new_game_title);
        save = (Button) findViewById(R.id.new_game_save_button);

        startdate.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(v == startdate && hasFocus){
                    setStartData();
                }

            }
        });

        startdate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                setStartData();

            }
        });

        enddate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                setEndData();

            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validation()){
                    makeGame();
                }

            }
        });

    }

}
