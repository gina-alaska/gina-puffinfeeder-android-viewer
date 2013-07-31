package edu.alaska.gina.feeder.puffinfeeder;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
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
public class ImageFeedFragment extends SherlockFragment {
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_image_feed, container, false);
        setHasOptionsMenu(true);

        Bundle extras = getArguments();
        imageFeed.setTitle(extras.getString("title"));
        imageFeed.setStatusBoolean(extras.getBoolean("status"));
        imageFeed.setEntries(extras.getString("entries"));
        imageFeed.setSlug(extras.getString("slug"));
        JSON_CACHE_KEY = imageFeed.getSlug() + "_json";

        getSherlockActivity().setSupportProgressBarIndeterminateVisibility(true);

        mSpiceManager.execute(new FeedImagesJsonRequest(imageFeed, 1), JSON_CACHE_KEY, DurationInMillis.ALWAYS_EXPIRED, new ImageFeedRequestListener());
        mImageAdapter = new PicassoImageAdapter(this.getActivity(), mList);

        getSherlockActivity().getSupportActionBar().setTitle(imageFeed.getTitle());

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        mSpiceManager.start(this.getActivity());

        GridView gridView = (GridView) getActivity().findViewById(R.id.image_grid);
        gridView.setAdapter(mImageAdapter);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent photoView = new Intent(getSherlockActivity(), ImageViewFrameActivty.class);

                ArrayList<String> times = new ArrayList<String>();
                for (DateTime d : mTimes)
                    times.add(d.toString());

                Bundle args = new Bundle();
                args.putAll(encodeBundle(mUrls, "url", 3));
                args.putAll(encodeBundle(mTitles, "title"));
                args.putAll(encodeBundle(times, "time"));
                args.putString("feed_name", imageFeed.getTitle());
                args.putInt("position", position);

                photoView.putExtras(args);

                getSherlockActivity().startActivity(photoView);
            }
        });

        mImageAdapter.notifyDataSetChanged();
    }

    @Override
    public void onStop() {
        mSpiceManager.shouldStop();
        super.onStop();
    }

    public void refreshThumbs(boolean isNew, boolean isNext) {
        getSherlockActivity().setSupportProgressBarIndeterminateVisibility(true);

        if (isNew) {
            if (isNext)
                page++;
            else
                page--;
        }
        else
            if (!isNext)
                page = 1;

        mSpiceManager.execute(new FeedImagesJsonRequest(imageFeed, page), JSON_CACHE_KEY, DurationInMillis.ALWAYS_EXPIRED, new ImageFeedRequestListener());
    }

    public Bundle encodeBundle(ArrayList<String> notEncoded, String key) {
        Bundle encoded = new Bundle();

        for (int i = 0; i < notEncoded.size(); i++)
            encoded.putString("image_" + key + "_" + i, notEncoded.get(i));

        return encoded;
    }

    public Bundle encodeBundle(ArrayList<String[]> notEncoded, String key, int numSizes) {
        Bundle encoded = new Bundle();
        encoded.putInt("num_image_sizes", numSizes);

        for (int i = 0; i < notEncoded.size(); i++)
            for (int j = 0; j < numSizes; j++)
                encoded.putString("image_" + key + "_" + i + "_" + j, notEncoded.get(i)[j]);

        return encoded;
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

    private class ImageFeedRequestListener implements RequestListener<FeedImage[]> {

        @Override
        public void onRequestFailure(SpiceException spiceException) {
            Log.d(getString(R.string.app_tag), "Image Feed load fail! " + spiceException.getMessage());
            Toast.makeText(getActivity(), "Image Feed load fail!", Toast.LENGTH_SHORT).show();
            getSherlockActivity().setSupportProgressBarIndeterminateVisibility(false);
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
                    Log.d(getString(R.string.app_tag), "Stamp: " + mTimes.get(mTimes.size() - 1));
                }

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mImageAdapter.notifyDataSetChanged();
                        getSherlockActivity().setSupportProgressBarIndeterminateVisibility(false);
                    }
                });
            }
        }
    }
}
