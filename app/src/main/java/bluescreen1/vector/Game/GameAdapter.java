package bluescreen1.vector.Game;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import bluescreen1.vector.R;

/**
 * Created by Dane on 5/10/2016.
 */
public class GameAdapter extends ArrayAdapter<JSONObject> {

    private final Context context;
    private final ArrayList<JSONObject> values;

    public GameAdapter(Context context, ArrayList<JSONObject> objects) {
        super(context, -1, objects);
        this.context = context;
        this.values = objects;
    }


    @Override
    public JSONObject getItem(int position) {
        return values.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        JSONObject a = values.get(position);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.game_item, parent, false);
        }
        TextView id = (TextView) convertView.findViewById(R.id.game_item_id);
        TextView title = (TextView) convertView.findViewById(R.id.game_item_title);
        TextView status = (TextView) convertView.findViewById(R.id.game_item_status);
        TextView starttime = (TextView) convertView.findViewById(R.id.game_item_start_time);

        try {
            id.setText(a.get("id").toString());
            title.setText(a.get("name").toString());
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(context, "OH WELL"+ position, Toast.LENGTH_LONG ).show();
        }

        // change the icon for Windows and iPhone
        return convertView;
    }
}
