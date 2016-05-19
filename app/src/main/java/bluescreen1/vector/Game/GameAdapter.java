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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import bluescreen1.vector.R;

/**
 * Created by Dane on 5/10/2016.
 */
public class GameAdapter extends ArrayAdapter<JSONObject> {

    private final Context context;
    private final ArrayList<JSONObject> values;
    TextView status;
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
        status = (TextView) convertView.findViewById(R.id.game_item_status);
        TextView starttime = (TextView) convertView.findViewById(R.id.game_item_start_time);

        try {
            id.setText(a.get("id").toString());
            title.setText(a.get("name").toString());
            String start_string = a.getString("start_time");

            String[] start_datetime = start_string.split("T");
            String start_text = start_datetime[0] + " " +
                    start_datetime[1].substring(0, start_datetime[1].length()-5);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault());
            Date start = dateFormat.parse(start_text);
            Calendar calendar = Calendar.getInstance();
            DateFormat df = DateFormat.getDateTimeInstance();

            calendar.setTime(start);
            calendar.add(Calendar.HOUR_OF_DAY, -5);
            starttime.setText(df.format(calendar.getTime()));
            status.setText(getstatus(start_string, a.getString("end_time")));
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(context, "OH WELL"+ position, Toast.LENGTH_LONG ).show();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // change the icon for Windows and iPhone
        return convertView;
    }

    protected String getstatus(String start_string, String end_string){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault());
        String[] start_datetime = start_string.split("T");
        String[] end_datetime = end_string.split("T");
        String start_text = start_datetime[0] + " " + start_datetime[1].substring(0, start_datetime[1].length()-5);
        String end_text = end_datetime[0] + " " + end_datetime[1].substring(0, end_datetime[1].length()-5);
        Date start = get_date_d(start_string);
        Date end = get_date_d(end_string);
        Date now = new Date();
        if(start.after(now)){
            status.setTextColor(context.getResources().getColor(R.color.red));
            return "NOT STARTED";
        } else {
            if(end.after(now)){
                status.setTextColor(context.getResources().getColor(R.color.grassgreen));

                return "RUNNING";
            } else {
                status.setTextColor(context.getResources().getColor(R.color.sand));

                return "ENDED";
            }
        }

    }

    protected Date get_date_d(String date){

        String[] date_datetime = date.split("T");
        String date_text = date_datetime[0] + " " +
                date_datetime[1].substring(0, date_datetime[1].length()-5);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault());
        Date start = new Date();
        try {
            start = dateFormat.parse(date_text);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        DateFormat df = DateFormat.getDateTimeInstance();

        calendar.setTime(start);
        calendar.add(Calendar.HOUR_OF_DAY, -5);
        return calendar.getTime();
    }
}
