package edu.alaska.gina.feeder.puffinfeeder;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
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

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;

/**
 * Fragment used to display the list of feed images in a GridView.
 * Created by bobby on 6/14/13.
 */
public class ImageFeedFragment extends Fragment {
    private static String JSON_CACHE_KEY;
    protected SpiceManager mSpiceManager = new SpiceManager(JsonSpiceService.class);

    protected Menu aBarMenu;

    protected SharedPreferences sharedPreferences;

    protected Feed imageFeed = new Feed();
    protected ArrayList<FeedImage> mList = new ArrayList<FeedImage>();
    protected ArrayList<String> mTitles = new ArrayList<String>();
    protected ArrayList<String[]> mUrls = new ArrayList<String[]>();
    protected ArrayList<DateTime> mTimes = new ArrayList<DateTime>();
    protected PicassoImageAdapter mImageAdapter;
    private int page = 1;

    /** Overridden Methods. */

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_image_feed, container, false);
        setHasOptionsMenu(true);

        Bundle extras = getArguments();
        imageFeed.setTitle(extras.getString("title"));
        imageFeed.setStatusBoolean(extras.getBoolean("status"));
        imageFeed.setEntries(extras.getString("entries"));
        imageFeed.setSlug(extras.getString("slug"));
        imageFeed.setDescription(extras.getString("description"));
        imageFeed.setMoreinfo(extras.getString("info"));
        JSON_CACHE_KEY = imageFeed.getSlug() + "_json";

        getActivity().setProgressBarIndeterminateVisibility(true);

        refreshThumbs(false, false);
        mImageAdapter = new PicassoImageAdapter(this.getActivity(), mList);

        getActivity().getActionBar().setTitle(imageFeed.getTitle());

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();

        GridView gridView = (GridView) getActivity().findViewById(R.id.image_grid);
        gridView.setAdapter(mImageAdapter);
        adaptGridViewSize(gridView);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent photoView = new Intent(getActivity(), ImageViewFrameActivty.class);

                ArrayList<String> times = new ArrayList<String>();
                for (DateTime d : mTimes)
                    times.add(d.toString());

                Bundle args = new Bundle();
                args.putAll(encodeBundle(mUrls, "url", 3));
                args.putAll(encodeBundle(mTitles, "title"));
                args.putAll(encodeBundle(times, "time"));
                args.putString("description", imageFeed.getDescription());
                args.putString("info", imageFeed.getMoreinfo());
                args.putString("feed_name", imageFeed.getTitle());
                args.putInt("position", position);

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
        adaptGridViewSize((GridView) getActivity().findViewById(R.id.image_grid));
    }

    /** Class to run after RoboSpice task completion. */
    private class ImageFeedRequestListener implements RequestListener<FeedImage[]> {

        @Override
        public void onRequestFailure(SpiceException spiceException) {
            Log.d(getString(R.string.app_tag), "Image Feed load fail! " + spiceException.getMessage());
            Toast.makeText(getActivity(), "Image Feed load fail!", Toast.LENGTH_SHORT).show();
            getActivity().setProgressBarIndeterminateVisibility(false);
            mSpiceManager.shouldStop();
        }

        @Override
        public void onRequestSuccess(FeedImage[] feedImages) {
            if (page <= 1) {
                aBarMenu.findItem(R.id.action_load_prev).setIcon(R.drawable.ic_navigation_back_dud);
                aBarMenu.findItem(R.id.action_load_first).setIcon(R.drawable.ic_navigation_first_dud);
            }
            else {
                aBarMenu.findItem(R.id.action_load_prev).setIcon(R.drawable.ic_navigation_back);
                aBarMenu.findItem(R.id.action_load_first).setIcon(R.drawable.ic_navigation_first);
            }

            if (mList.size() > 0 && !feedImages[0].equals(mList.get(0)))
                mList.clear();

            if (mList.size() <= 0) {
                for (FeedImage pii : feedImages)
                    mList.add(pii);

                DateTimeFormatter formatter = DateTimeFormat.forPattern(getString(R.string.event_at_pattern));

                mTitles.clear();
                mUrls.clear();
                mTimes.clear();
                for (FeedImage f : mList) {
                    mTitles.add(f.getTitle());
                    mUrls.add(f.getPreviews().getAll());
                    mTimes.add(formatter.parseDateTime(f.getEvent_at()));
                }

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mImageAdapter.notifyDataSetChanged();
                        getActivity().setProgressBarIndeterminateVisibility(false);
                    }
                });

                mSpiceManager.shouldStop();
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
    public void refreshThumbs(boolean isNew, boolean isNext) {
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

        mSpiceManager.execute(new FeedImagesJsonRequest(imageFeed, page), JSON_CACHE_KEY, DurationInMillis.ALWAYS_EXPIRED, new ImageFeedRequestListener());
    }

    /**
     * Takes an ArrayList of Strings and puts them in a bundle.
     * Key Format: image_[key]_[num]
     * @param notEncoded ArrayList of Strings to be bundled.
     * @param key String used as part of the identifier in the bundle.
     * @return Bundle containing all Strings from initial ArrayList.
     */
    public Bundle encodeBundle(ArrayList<String> notEncoded, String key) {
        Bundle encoded = new Bundle();

        for (int i = 0; i < notEncoded.size(); i++)
            encoded.putString("image_" + key + "_" + i, notEncoded.get(i));

        return encoded;
    }

    /**
     * Takes an ArrayList of String Arrays and puts them in a bundle.
     * Key Format: image_[key]_[image num]_[size num]
     * @param notEncoded ArrayList of String Arrays to be bundled.
     * @param key String used as part of the identifier in the bundle.
     * @param numSizes Number of items in each String Array.
     * @return Bundle containing all Strings from initial ArrayList.
     */
    public Bundle encodeBundle(ArrayList<String[]> notEncoded, String key, int numSizes) {
        Bundle encoded = new Bundle();
        encoded.putInt("num_image_sizes", numSizes);

        for (int i = 0; i < notEncoded.size(); i++)
            for (int j = 0; j < numSizes; j++)
                encoded.putString("image_" + key + "_" + i + "_" + j, notEncoded.get(i)[j]);

        return encoded;
    }

    /** Methods used to dynamically adapt GridView thumbnail sizing. */

    /**
     * Method containing the logic behind dynamic resizing.
     * @param gv GridView to be adjusted.
     */
    public void adaptGridViewSize(GridView gv) {
        int thumbMax = 250;
        int spacing = 2;

        DisplayMetrics d = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(d);

        float trueMaxThumbWidth = maxTW(2, spacing, d);
        if (trueMaxThumbWidth <= thumbMax) {
            gv.setNumColumns(2);
            gv.setColumnWidth((int) trueMaxThumbWidth);
            gv.setHorizontalSpacing(spacing);
            return;
        }

        int numCols;
        float maxCols = numCols(thumbMax, spacing, d);
        if (maxCols - ((int) maxCols) > 0.5)
            numCols = ((int) maxCols) + 1;
        else
            numCols = ((int) maxCols);

        gv.setHorizontalSpacing(spacing);
        gv.setVerticalSpacing(spacing);

        int tWidth = (int) maxTW(numCols, spacing, d);
        gv.setColumnWidth(tWidth);

        gv.setNumColumns(numCols);
    }

    /**
     * Calculates the maximum thumbnail width given number of columns, spacing, and display size.
     * @param numCols Number of columns to calculate for.
     * @param spacing Space between images in regular pixels (px).
     * @param d Screen information (in DisplayMetrics object).
     * @return Maximum width of thumbnails (px) given parameters.
     */
    public float maxTW(float numCols, float spacing, DisplayMetrics d) {
        return (d.widthPixels - ((numCols + 1) * spacing)) / numCols;
    }

    /**
     * Calculates the number of columns possible given thumbnail width (px), spacing (px),
     * and screen dimensions.
     * @param thumbWidth Width of the thumbnails (px).
     * @param spacing Space between images (px).
     * @param d Screen information (in DisplayMetrics object).
     * @return Maximum number of columns given parameters.
     */
    public float numCols(float thumbWidth, float spacing, DisplayMetrics d) {
        return (d.widthPixels - spacing) / (spacing + thumbWidth);
    }
}
