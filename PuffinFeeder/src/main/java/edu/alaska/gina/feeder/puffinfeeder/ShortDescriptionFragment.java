package edu.alaska.gina.feeder.puffinfeeder;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.widget.Toast;

/**
 * Created by bobby on 6/25/13.
 */
public class ShortDescriptionFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        String description = args.getString("description");
        String title = args.getString("title");
        //String slug = args.getString("slug");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title).setMessage(description).setNeutralButton("Do Nothing", null);

        return builder.create();
    }
}
