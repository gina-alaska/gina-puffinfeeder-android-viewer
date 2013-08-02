package edu.alaska.gina.feeder.puffinfeeder;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockDialogFragment;

/**
 * Small Dialog that displays the description of the currently selected feed.
 * Created by bobby on 6/25/13.
 */
public class ShortDescriptionFragment extends SherlockDialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        String description = args.getString("description");
        final String title = args.getString("title");
        final String detailedUrl = args.getString("url");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        if (detailedUrl == null || detailedUrl.equals("")) {
            builder.setTitle(title).setMessage(description).setNeutralButton("Dismiss", null);
        }
        else {
            builder.setTitle(title).setMessage(description).setPositiveButton("More Info", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent descrip = new Intent(getActivity(), WebViewActivity.class);
                    descrip.putExtra("url", detailedUrl).putExtra("title", title);
                    getSherlockActivity().startActivity(descrip);
                }
            }).setNeutralButton("Dismiss", null);
        }

        return builder.create();
    }
}
