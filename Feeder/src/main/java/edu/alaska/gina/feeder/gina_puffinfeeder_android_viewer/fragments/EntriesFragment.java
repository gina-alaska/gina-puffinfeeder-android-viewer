package edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
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
import edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer.FullscreenImageViewerActivity;
import edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer.R;
import edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer.adapters.EntriesAdapter;
import edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer.network.JSONRequest;
import edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer.network.JsonSpiceService;

/**
 * Fragment used to display the list of feed images in a GridView.
 * Created by bobby on 6/14/13.
 */
public class EntriesFragment extends Fragment {
    private final SpiceManager mSpiceManager = new SpiceManager(JsonSpiceService.class);

    private int fadeAnimationDuration;
    private View loadingView, contentView;

    private ContentDataFragment data;
    private String currentURL;
    private EntriesAdapter mImageAdapter;

    /* Overridden Methods. */

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_entries, container, false);
        setHasOptionsMenu(true);

        Bundle extras = getArguments();
        this.currentURL = extras.getString("entries");

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        fadeAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);
        loadingView = getActivity().findViewById(R.id.grid_progressBar);
        contentView = getActivity().findViewById(R.id.image_grid);

        this.data = (ContentDataFragment) getActivity().getFragmentManager().findFragmentByTag(getString(R.string.content_retained_tag));
        if (this.data == null) {
            this.data = new ContentDataFragment();
            getActivity().getFragmentManager().beginTransaction().add(this.data, getString(R.string.content_retained_tag)).commit();
            this.data.retainedURL = this.currentURL;
            this.data.entries = new ArrayList<Entry>(12);
            this.mImageAdapter = new EntriesAdapter(this.getActivity(), data.entries);
            networkRequest();
        } else if (!this.data.retainedURL.equals(this.currentURL)) {
            this.data.retainedURL = this.currentURL;
            this.data.entries = new ArrayList<Entry>(12);
            this.mImageAdapter = new EntriesAdapter(this.getActivity(), data.entries);
            networkRequest();
        } else {
            mImageAdapter = new EntriesAdapter(this.getActivity(), data.entries);
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

        GridView gridView = (GridView) contentView;
        gridView.setAdapter(mImageAdapter);
        gridView.setGravity(Gravity.CENTER_HORIZONTAL);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent photoView = new Intent(getActivity(), FullscreenImageViewerActivity.class);

                Bundle args = new Bundle();
                args.putSerializable("entry", data.entries.get(position));
                photoView.putExtras(args);

                getActivity().startActivity(photoView);
            }
        });

        mImageAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDetach() {
        if (mSpiceManager.isStarted())
            mSpiceManager.shouldStop();
        super.onDetach();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                networkRequest();
                return true;
        }
        return super.onOptionsItemSelected(item);
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
            if (data.entries.size() > 0 && !entries[0].equals(data.entries.get(0)))
                data.entries.clear();

            if (data.entries.size() <= 0)
                Collections.addAll(data.entries, entries);

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

        mSpiceManager.execute(new JSONRequest<Entry[]>(Entry[].class, currentURL + "?count=24"), getString(R.string.entries_cache), DurationInMillis.ALWAYS_EXPIRED, new ImageFeedRequestListener());
    }

    public static class ContentDataFragment extends Fragment {
        String retainedURL;
        ArrayList<Entry> entries;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
        }
    }
}
