package bluescreen1.vector.User;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import bluescreen1.vector.Config;
import bluescreen1.vector.Game.DetailsActivity;
import bluescreen1.vector.Game.GameAdapter;
import bluescreen1.vector.Game.GameDB;
import bluescreen1.vector.Models.UserEntry;
import bluescreen1.vector.R;
import bluescreen1.vector.VectorApplication;

/**
 * Created by Dane on 5/11/2016.
 */
public class DiscoverActivity extends AppCompatActivity {
    GameAdapter gameAdapter;
    ListView gamelistview;
    int userid;
    EditText searchbar;
    ArrayList<JSONObject> jobj;
    String token;

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
    }

    private void search(){
        String term = searchbar.getText().toString();
//        toastit("" + jobj.size());
        ArrayList<JSONObject> newjobj = new ArrayList<>();
        if (term.trim().equals("")){
            toastit("Enter a term.");
        } else {
            for (JSONObject j : (ArrayList<JSONObject>)jobj.clone()){
                try {
                    if (j.getString("name").toLowerCase().contains(term.toLowerCase().trim())){
                        newjobj.add(j);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
//            if(newjobj.size() >= jobj.size()){
//                jobj = newjobj.clone();
//            }
            gameAdapter.clear();
            gameAdapter.addAll(newjobj);
            gameAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_activity);
        setData();
        gamelistview = (ListView) findViewById(R.id.available_games);
        final Intent details = new Intent(this, DetailsActivity.class);
        searchbar = (EditText) findViewById(R.id.search_activity_bar);
        Button searchButton = (Button) findViewById(R.id.search_activity_search_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search();
            }
        });
        gamelistview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                JSONObject game = gameAdapter.getItem(position);
                //
//                    toastit(""+game.getInt("id"));
                details.putExtra("game", game.toString());
                details.putExtra("in",0);
                startActivity(details);

            }
        });
        getData(userid);

    }

    private void toastit(String text){
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();

    }

    private void getData(int userid){
        String url = Config.APPLICATION_SERVER_URL + "games?available=true";
//        toastit(url);
        final Context context= this;

        JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(url,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray ja) {
                       jobj = new ArrayList<>();
                        try {

                            toastit(""+ja.length());
                            for( int x = 0; x < ja.length(); x++){
                                jobj.add(ja.getJSONObject(x));
                            }
                            gameAdapter = new GameAdapter(context, jobj);
                            gamelistview.setAdapter(gameAdapter);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_games, menu);
        return true;
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        getData(userid);
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

}

