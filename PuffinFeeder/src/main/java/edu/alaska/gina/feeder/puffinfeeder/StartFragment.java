package edu.alaska.gina.feeder.puffinfeeder;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;

/**
 * Fragment displayed on startup.
 * Created by bobby on 6/21/13.
 */
public class StartFragment extends SherlockFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_start, container, false);
        getSherlockActivity().getSupportActionBar().setTitle("GINA Puffin Feeder");

        return v;
    }
}
