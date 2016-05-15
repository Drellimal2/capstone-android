package bluescreen1.vector;

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

/**
 * Created by Dane on 5/13/2016.
 */
public class CluesAdapter  extends ArrayAdapter<JSONObject> {

    private final Context context;
    private final ArrayList<JSONObject> values;

    public CluesAdapter(Context context, ArrayList<JSONObject> objects) {
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
            convertView = inflater.inflate(R.layout.clue_item, parent, false);
        }
        TextView id = (TextView) convertView.findViewById(R.id.clue_item_id);
        TextView question = (TextView) convertView.findViewById(R.id.clue_item_question);
        TextView answer = (TextView) convertView.findViewById(R.id.clue_item_answer);
        TextView hint = (TextView) convertView.findViewById(R.id.clue_item_hint);

        try {
            id.setText(""+a.getInt("id"));
            question.setText(a.getString("question"));
            answer.setText(a.getString("answer"));
            hint.setText(a.getString("hint"));
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(context, "OH WELL"+ position, Toast.LENGTH_LONG ).show();
        }

        // change the icon for Windows and iPhone
        return convertView;
    }
}
