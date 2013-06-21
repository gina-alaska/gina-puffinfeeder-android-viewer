package edu.alaska.gina.feeder.puffinfeeder;

import android.content.Context;
import android.widget.ArrayAdapter;

import java.util.List;

/**
 * Currently unused Adapter for adding online indicators to the drawer in the future.
 * Created by bobby on 6/20/13.
 */
public class DrawerListAdapter extends ArrayAdapter<String> {
    public DrawerListAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public DrawerListAdapter(Context context, int resource, int textViewResourceId) {
        super(context, resource, textViewResourceId);
    }

    public DrawerListAdapter(Context context, int textViewResourceId, String[] objects) {
        super(context, textViewResourceId, objects);
    }

    public DrawerListAdapter(Context context, int resource, int textViewResourceId, String[] objects) {
        super(context, resource, textViewResourceId, objects);
    }

    public DrawerListAdapter(Context context, int textViewResourceId, List<String> objects) {
        super(context, textViewResourceId, objects);
    }

    public DrawerListAdapter(Context context, int resource, int textViewResourceId, List<String> objects) {
        super(context, resource, textViewResourceId, objects);
    }
}
