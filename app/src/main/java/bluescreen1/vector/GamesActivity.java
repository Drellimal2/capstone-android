package bluescreen1.vector;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import bluescreen1.vector.Game.GameDB;
import bluescreen1.vector.Models.UserEntry;

public class GamesActivity extends AppCompatActivity {

    String game;
    private SectionsPagerAdapter mSectionsPagerAdapter;

    FloatingActionButton fab;
    private ViewPager mViewPager;
    int userid;
    String token;
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
        setContentView(R.layout.activity_games);
        setData();
        game = getIntent().getStringExtra("game");
        try {
            jgame = new JSONObject(game);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        GameDetailsFragment gd = GameDetailsFragment.newInstance(game);
        CluesFragment cf = CluesFragment.newInstance(game);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), gd, cf);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GamesActivity.this, NewClue.class);
                intent.putExtra("GAME", game);
                startActivity(intent);

            }
        });

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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        GameDetailsFragment gd;
        CluesFragment cf;
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public SectionsPagerAdapter(FragmentManager fm, GameDetailsFragment g, CluesFragment c) {
            super(fm);
            gd=g;
            cf = c;
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            if (position == 0){
                fab.setVisibility(View.GONE);
                return gd;

            }else if(position == 1) {

                fab.setVisibility(View.VISIBLE);
                return cf;
            }
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Details";
                case 1:
                    return "Clues";

            }
            return null;
        }
    }

    public static class PlaceholderFragment extends Fragment {

        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_games, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }

    public static class CluesFragment extends Fragment {

        private static final String ARG_SECTION_NUMBER = "section_number";
        private static final String GAME = "game";
        CluesAdapter cluesAdapter;
        ListView clues;

        public CluesFragment() {
        }

        private void getData(){
            String url = Config.GAME_URL + getArguments().getInt(GAME) + "/clues/";


            final Context context= getActivity();
//            Toast.makeText(context, url, Toast.LENGTH_LONG).show();
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
                                cluesAdapter = new CluesAdapter(context, jobj);
                                clues.setAdapter(cluesAdapter);
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

        public static CluesFragment newInstance(String game){
            CluesFragment fragment = new CluesFragment();
            Log.i("HOOOOO", game);
            Bundle args = new Bundle();
            JSONObject jgame = null;
            try {
                jgame = new JSONObject(game);
                args.putInt(GAME, jgame.getInt("id"));
                fragment.setArguments(args);
                Log.i("HOOOOO", ""+jgame.getInt("id"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return fragment;
        }


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.clues, container, false);

            clues = (ListView) rootView.findViewById(R.id.game_clues);
            final Context context= getActivity();

//            Toast.makeText(getContext(),"" + getArguments().getInt(GAME), Toast.LENGTH_LONG).show();
//            cluesAdapter = new CluesAdapter(getActivity(), new ArrayList<JSONObject>());
//            clues.setAdapter(cluesAdapter);
            getData();
            return rootView;
        }




    }

    public static class GameDetailsFragment extends Fragment {

        private static final String ARG_SECTION_NUMBER = "section_number";
        private static final String GAME = "game";

        public GameDetailsFragment() {
        }

        public static GameDetailsFragment newInstance(String game) {
            GameDetailsFragment fragment = new GameDetailsFragment();
            Bundle args = new Bundle();
            args.putString(GAME, game);
            fragment.setArguments(args);

            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.details_game, container, false);
            TextView title = (TextView) rootView.findViewById(R.id.game_details_title);
            TextView start_time = (TextView) rootView.findViewById(R.id.game_details_start_time);
            TextView end_time = (TextView) rootView.findViewById(R.id.game_details_end_time);
            TextView status = (TextView) rootView.findViewById(R.id.game_details_status);
            Button button = (Button) rootView.findViewById(R.id.game_details_button);
            button.setVisibility(View.GONE);
            final TextView countdown = (TextView) rootView.findViewById(R.id.game_details_countdown);
            TextView desc = (TextView) rootView.findViewById(R.id.game_details_desc);
            String sgame = getArguments().getString(GAME);
//            Toast.makeText(getActivity(), sgame, Toast.LENGTH_LONG).show();
            try {
                JSONObject jgame = new JSONObject(sgame);
                title.setText(jgame.getString("name"));
                String start_string = jgame.getString("start_time");
                String[] start_datetime = start_string.split("T");
                String start_text = start_datetime[0] + " " +
                        start_datetime[1].substring(0, start_datetime[1].length()-5);
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault());
                Date start = dateFormat.parse(start_text);
                start_time.setText(start.toString());
                String end_string = jgame.getString("end_time");
                String[] end_datetime = start_string.split("T");
                String end_text = start_datetime[0] + " @ " +
                        end_datetime[1].substring(0, end_datetime[1].length()-5);
                end_time.setText(end_text);
                desc.setText(jgame.getString("description"));
            } catch (JSONException | ParseException e) {
                e.printStackTrace();
            }


            return rootView;
        }
    }
}
