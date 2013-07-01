package edu.alaska.gina.feeder.puffinfeeder;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.widget.Toast;

/**
 * Small Dialog that displays the description of the currently selected feed.
 * Created by bobby on 6/25/13.
 */
public class ShortDescriptionFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        String description = args.getString("description");
        String title = args.getString("title");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title).setMessage(description).setNeutralButton("Dismiss", null);

        return builder.create();
    }
}
