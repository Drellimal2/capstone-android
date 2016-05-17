package bluescreen1.vector;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import bluescreen1.vector.Game.DetailsActivity;
import bluescreen1.vector.Game.GameAdapter;
import bluescreen1.vector.Game.GameDB;
import bluescreen1.vector.Game.NewGameActivity;
import bluescreen1.vector.Models.UserEntry;
import bluescreen1.vector.User.DiscoverActivity;
import bluescreen1.vector.User.LoginActivity;

public class MainActivity extends AppCompatActivity {

    GameAdapter gameAdapter;
    ListView gamelistview;
    int userid;
    String token;
    String ptype;


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
        setContentView(R.layout.mygames);
        setData();
        final Intent discover = new Intent(this, DiscoverActivity.class);
        final Intent newGame = new Intent(this, NewGameActivity.class);
        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if(ptype.equals("1")) {
            fab.setImageDrawable(getResources().getDrawable(R.mipmap.ic_search));
        }
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ptype.equals("1")) {
                    startActivity(discover);
                } else {
                    startActivity(newGame);
                }

            }
        });
        gamelistview = (ListView) findViewById(R.id.home_user_games);
        final Intent details = new Intent(this, DetailsActivity.class);
        final Intent creator_details = new Intent(this, GamesActivity.class);
//        int userid = getIntent().getExtras().getInt("userid");
        getData(userid);
        gamelistview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                JSONObject game = gameAdapter.getItem(position);
                //
                if(ptype.equals("1")) {
                    details.putExtra("game", game.toString());
                    details.putExtra("in", 1);
                    startActivity(details);
                } else {
                    creator_details.putExtra("game", game.toString());
                    startActivity(creator_details);
                }
            }
        });
    }

    public void updateMessage(String deviceToken){
        Toast.makeText(this, deviceToken, Toast.LENGTH_LONG).show();
    }

    private void toastit(String text){
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();

    }

    private void getData(int userid){
        String url = Config.GAME_URL;
        if (ptype.equals("1")) {
            url = Config.APPLICATION_SERVER_URL + "users/" + userid + "/games/";
        } else {
            url = Config.APPLICATION_SERVER_URL + "users/" + userid + "/created_games/";
        }

        final Context context= this;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        JSONArray ja = null;
                        ArrayList<JSONObject> jobj = new ArrayList<>();
                        try {
                            ja = jsonObject.getJSONArray("message");
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
            }
        });

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
}



