package edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer.R;

/**
 * Fragment displayed on startup.
 * Created by bobby on 6/21/13.
 */
public class StartFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_start, container, false);
        if (getActivity().getActionBar() != null)
            getActivity().getActionBar().setTitle("GINA Puffin Feeder");

        return v;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
