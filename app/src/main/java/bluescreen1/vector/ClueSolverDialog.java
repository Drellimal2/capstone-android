package bluescreen1.vector;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Dane on 5/14/2016.
 */
public class ClueSolverDialog  extends DialogFragment {




    public interface NoticeDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog, boolean b, int id);
        public void onDialogNegativeClick(DialogFragment dialog);
    }


    private JSONObject jclue;

    // Use this instance of the interface to deliver action events
    NoticeDialogListener mListener;

    public static ClueSolverDialog newInstance(JSONObject clue){
        Bundle args = new Bundle();
        ClueSolverDialog clueD = new ClueSolverDialog();
        try {
            args.putString("question", clue.getString("question"));
            args.putString("answer", clue.getString("answer"));
            args.putString("hint", clue.getString("hint"));
            args.putInt("cid", clue.getInt("id"));

            clueD.setArguments(args);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return clueD;
    }

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (NoticeDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }

//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        // Inflate the layout to use as dialog or embedded fragment
//        return inflater.inflate(R.layout.solve_clue_dialog, container, false);
//    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View root = inflater.inflate(R.layout.solve_clue_dialog, null);
        // Inflate and set the layout for the dialog
        TextView question = (TextView) root.findViewById(R.id.solve_clue_question);
        question.setText(getArguments().getString("question"));
        TextView correct_answer = (TextView) root.findViewById(R.id.solve_clue_correct_answer);
        correct_answer.setText(getArguments().getString("answer"));
        final EditText answer = (EditText) root.findViewById(R.id.solve_clue_answer);
        answer.setHint(getArguments().getString("hint"));
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(root)
                // Add action buttons
                .setPositiveButton("Solve", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        if(answer.getText().toString().toLowerCase()
                            .equals(getArguments().getString("answer").toLowerCase())){
                            mListener.onDialogPositiveClick(ClueSolverDialog.this, true,
                                    getArguments().getInt("cid"));
                        } else {
                            mListener.onDialogPositiveClick(ClueSolverDialog.this, false,
                                    getArguments().getInt("id"));
                        }

                        // sign in the user ...
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ClueSolverDialog.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }
}
