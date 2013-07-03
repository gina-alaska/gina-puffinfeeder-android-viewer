package edu.alaska.gina.feeder.puffinfeeder;

import android.content.Intent;
import android.os.Bundle;
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

import java.util.ArrayList;

/**
 * Fragment used to display the list of feed images in a GridView.
 * Created by bobby on 6/14/13.
 */
public class ImageFeedFragment extends SherlockFragment {
    private static String JSON_CACHE_KEY;
    protected SpiceManager mSpiceManager = new SpiceManager(JsonSpiceService.class);

    protected Menu aBarMenu;
    protected MenuItem menuItem;

    protected Feed imageFeed = new Feed();
    protected ArrayList<FeedImage> mList = new ArrayList<FeedImage>();
    protected ArrayList<String> mTitles = new ArrayList<String>();
    protected ArrayList<String> mUrls = new ArrayList<String>();
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
        mImageAdapter = new PicassoImageAdapter(this.getActivity(), this.getSherlockActivity(), mList);

        getSherlockActivity().getSupportActionBar().setTitle(imageFeed.getTitle());

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        mSpiceManager.start(this.getActivity());

        GridView gridView = (GridView) getActivity().findViewById(R.id.image_grid);
        gridView.setAdapter(mImageAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent photoView = new Intent(getSherlockActivity(), ImageViewFrameActivty.class);

                Bundle args = new Bundle();
                args.putAll(encodeBundle(mUrls, "url"));
                args.putAll(encodeBundle(mTitles, "title"));
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

    public void refreshThumbs() {
        //menuItem = aBarMenu.findItem(R.id.action_load_more);
        //menuItem.setActionView(R.layout.actionbar_progress_bar);
        //menuItem.expandActionView();
        getSherlockActivity().setSupportProgressBarIndeterminateVisibility(true);

        page++;
        mSpiceManager.execute(new FeedImagesJsonRequest(imageFeed, page), JSON_CACHE_KEY, DurationInMillis.ALWAYS_EXPIRED, new ImageFeedRequestListener());
    }

    public Bundle encodeBundle(ArrayList<String> notEncoded, String key) {
        Bundle encoded = new Bundle();

        for (int i = 0; i < notEncoded.size(); i++)
            encoded.putString("image_" + key + "_" + i, notEncoded.get(i));

        return encoded;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        aBarMenu = menu;
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_load_more:
                refreshThumbs();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class ImageFeedRequestListener implements RequestListener<FeedImage[]> {

        @Override
        public void onRequestFailure(SpiceException spiceException) {
            menuItem.collapseActionView();
            menuItem.setActionView(null);

            Log.d("Feeder Viewer", "Image Feed load fail!" + spiceException.getMessage());
            Toast.makeText(getActivity().getApplicationContext(), "Image Feed load fail!", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onRequestSuccess(FeedImage[] feedImages) {
            if (menuItem != null) {
                menuItem.collapseActionView();
                menuItem.setActionView(null);
            }

            for (FeedImage pii : feedImages)
                mList.add(pii);

            mTitles.clear();
            mUrls.clear();
            for (FeedImage f : mList) {
                mTitles.add(f.getTitle());
                mUrls.add(f.getImage());
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
