package edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Fragment;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import java.util.ArrayList;
import java.util.Collections;

import edu.alaska.gina.feeder.android.core.data.Entry;
import edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer.adapter.EntriesAdapter;
import edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer.network.JSONRequest;
import edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer.network.JsonSpiceService;

/**
 * Fragment used to display the list of feed images in a GridView.
 * Created by bobby on 6/14/13.
 */
public class ImageFeedFragment extends Fragment {
    private final SpiceManager mSpiceManager = new SpiceManager(JsonSpiceService.class);

    private Menu aBarMenu;
    private int fadeAnimationDuration;
    private View loadingView, contentView;

    private final ArrayList<Entry> entriesList = new ArrayList<Entry>();
    private String entriesURL;
    private EntriesAdapter mImageAdapter;
    private int page = 1;

    /* Overridden Methods. */

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_image_feed, container, false);
        setHasOptionsMenu(true);

        Bundle extras = getArguments();
        entriesURL = extras.getString("entries");

        mImageAdapter = new EntriesAdapter(this.getActivity(), entriesList);

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();

        fadeAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);
        loadingView = getActivity().findViewById(R.id.grid_progressBar);
        contentView = getActivity().findViewById(R.id.image_grid);

        networkRequest();

        GridView gridView = (GridView) contentView;
        gridView.setAdapter(mImageAdapter);
        gridView.setGravity(Gravity.CENTER_HORIZONTAL);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent photoView = new Intent(getActivity(), FullscreenImageViewerActivity.class);

                Bundle args = new Bundle();
                args.putSerializable("entry", entriesList.get(position));
                photoView.putExtras(args);

                getActivity().startActivity(photoView);
            }
        });

        mImageAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        if (mSpiceManager.isStarted())
            mSpiceManager.shouldStop();
        super.onPause();
    }

    @Override
    public void onStop() {
        if (mSpiceManager.isStarted())
            mSpiceManager.shouldStop();
        super.onStop();
    }

    @Override
    public void onDetach() {
        if (mSpiceManager.isStarted())
            mSpiceManager.shouldStop();
        super.onDetach();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        aBarMenu = menu;

        if (page <= 1)
            aBarMenu.findItem(R.id.action_load_prev).setIcon(R.drawable.ic_navigation_back_dud);
        else
            aBarMenu.findItem(R.id.action_load_prev).setIcon(R.drawable.ic_navigation_back);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_load_next:
                networkRequest();
                return true;
            case R.id.action_load_prev:
                if (page > 1)
                    networkRequest();
                return true;
            case R.id.action_refresh:
                networkRequest();
                return true;
            case R.id.action_load_first:
                if (page <= 1)
                    Toast.makeText(getActivity(), "Already on first page.", Toast.LENGTH_SHORT).show();
                networkRequest();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    /* Class to run after RoboSpice task completion. */
    public class ImageFeedRequestListener implements RequestListener<Entry[]> {
        @Override
        public void onRequestFailure(SpiceException spiceException) {
            Log.d(getString(R.string.app_tag), "Image Feed load fail! " + spiceException.getMessage());
            Toast.makeText(getActivity(), "Image Feed load fail!", Toast.LENGTH_SHORT).show();
            loadingView.setVisibility(View.GONE);
        }

        @Override
        public void onRequestSuccess(Entry[] entries) {
            if (page <= 1) {
                aBarMenu.findItem(R.id.action_load_prev).setIcon(R.drawable.ic_navigation_back_dud);
                aBarMenu.findItem(R.id.action_load_first).setIcon(R.drawable.ic_navigation_first_dud);
            } else {
                aBarMenu.findItem(R.id.action_load_prev).setIcon(R.drawable.ic_navigation_back);
                aBarMenu.findItem(R.id.action_load_first).setIcon(R.drawable.ic_navigation_first);
            }

            if (entriesList.size() > 0 && !entries[0].equals(entriesList.get(0)))
                entriesList.clear();

            if (entriesList.size() <= 0)
                Collections.addAll(entriesList, entries);

            mImageAdapter.notifyDataSetChanged();

            contentView.setAlpha(0f);
            contentView.setVisibility(View.VISIBLE);
            contentView.animate().alpha(1f).setDuration(fadeAnimationDuration).setListener(null);
            loadingView.animate().alpha(0f).setDuration(fadeAnimationDuration).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    loadingView.setVisibility(View.GONE);
                }
            });
        }
    }

    /**
     * Method that starts the network request for entries JSON file.
     */
    void networkRequest() {
        if (!mSpiceManager.isStarted())
            mSpiceManager.start(getActivity().getBaseContext());

        //((FeederFragmentInterface) getActivity()).networkRequest(new JSONRequest<Entry[]>(Entry[].class, entriesURL), getString(R.string.entries_cache), new ImageFeedRequestListener());
        mSpiceManager.execute(new JSONRequest<Entry[]>(Entry[].class, entriesURL), getString(R.string.entries_cache), DurationInMillis.ALWAYS_EXPIRED, new ImageFeedRequestListener());
    }
}
