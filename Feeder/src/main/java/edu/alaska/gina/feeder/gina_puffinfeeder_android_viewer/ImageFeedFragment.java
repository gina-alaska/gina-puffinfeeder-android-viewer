package edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer;

import android.app.Fragment;

import android.content.Intent;
import android.content.res.Configuration;

import android.os.Bundle;

import android.util.DisplayMetrics;
import android.util.Log;

import android.view.*;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer.adapter.EntriesAdapter;
import edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer.data.Entry;
import edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer.fragment.FeederFragmentInterface;
import edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer.network.JSONRequest;
import edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer.network.JsonSpiceService;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Fragment used to display the list of feed images in a GridView.
 * Created by bobby on 6/14/13.
 */
public class ImageFeedFragment extends Fragment {
    private final SpiceManager mSpiceManager = new SpiceManager(JsonSpiceService.class);

    private Menu aBarMenu;

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

        getActivity().setProgressBarIndeterminateVisibility(true);

        refreshThumbs(false, false);
        mImageAdapter = new EntriesAdapter(this.getActivity(), entriesList);

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();

        GridView gridView = (GridView) getActivity().findViewById(R.id.image_grid);
        gridView.setAdapter(mImageAdapter);
        gridView.setGravity(Gravity.CENTER_HORIZONTAL);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent photoView = new Intent(getActivity(), FullscreenImageViewerActivity.class);

                Bundle args = new Bundle();
                args.putString("url", entriesList.get(position).data_url);
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
                refreshThumbs(true, true);
                return true;
            case R.id.action_load_prev:
                if (page > 1)
                    refreshThumbs(true, false);
                return true;
            case R.id.action_refresh:
                refreshThumbs(false, true);
                return true;
            case R.id.action_load_first:
                if (page <= 1)
                    Toast.makeText(getActivity(), "Already on first page.", Toast.LENGTH_SHORT).show();
                refreshThumbs(false, false);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //adaptGridViewSize((GridView) getActivity().findViewById(R.id.image_grid));
    }

    /* Class to run after RoboSpice task completion. */
    public class ImageFeedRequestListener implements RequestListener<Entry[]> {
        @Override
        public void onRequestFailure(SpiceException spiceException) {
            Log.d(getString(R.string.app_tag), "Image Feed load fail! " + spiceException.getMessage());
            Toast.makeText(getActivity(), "Image Feed load fail!", Toast.LENGTH_SHORT).show();
            getActivity().setProgressBarIndeterminateVisibility(false);
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

            if (entriesList.size() <= 0) {
                Collections.addAll(entriesList, entries);

            mImageAdapter.notifyDataSetChanged();
            getActivity().setProgressBarIndeterminateVisibility(false);
            }
        }
    }

    /**
     * Method run to start to refresh the list of FeedImages on page reload or new page load.
     * (true, true) loads next page.
     * (true, false) loads the previous page.
     * (false, false) loads first page of results.
     * (false, true) reloads the same page.
     * @param isNew "true" if loading a new page. "false" otherwise.
     * @param isNext "true" is loading the next page. "false" otherwise.
     */
    void refreshThumbs(boolean isNew, boolean isNext) {
        getActivity().setProgressBarIndeterminateVisibility(true);

        if (!mSpiceManager.isStarted())
            mSpiceManager.start(getActivity().getBaseContext());

        if (isNew) {
            if (isNext)
                page++;
            else
                page--;
        }
        else if (!isNext)
            page = 1;

        //((FeederFragmentInterface) getActivity()).networkRequest(new JSONRequest<Entry[]>(Entry[].class, entriesURL), getString(R.string.entries_cache), new ImageFeedRequestListener());
        mSpiceManager.execute(new JSONRequest<Entry[]>(Entry[].class, entriesURL), getString(R.string.entries_cache), DurationInMillis.ALWAYS_EXPIRED, new ImageFeedRequestListener());
    }
}
