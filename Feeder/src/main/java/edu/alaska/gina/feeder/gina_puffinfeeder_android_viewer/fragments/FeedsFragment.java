package edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import edu.alaska.gina.feeder.android.core.data.Feed;
import edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer.R;
import edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer.adapters.FeedsAdapter;
import edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer.network.JSONRequest;
import edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer.network.JsonSpiceService;

/**
 * Fragment that handles displaying feed selection.
 * Created by Bobby on 7/2/2014.
 */
public class FeedsFragment extends Fragment {
    //TODO Write Feeds Fragment.

    private final SpiceManager networkManager = new SpiceManager(JsonSpiceService.class);

    private ProgressBar progressBar;
    private FeedsAdapter navAdapter;
    private ListView navList;
    private DrawerLayout navDrawer;
    private RelativeLayout infoDrawerLayout;

    private DrawerDataFragment data;
    private String baseURL;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        this.baseURL = getString(R.string.base_url);

        this.data = (DrawerDataFragment) getFragmentManager().findFragmentByTag("drawer_data");
        if (data == null) {
            this.data = new DrawerDataFragment();
            this.data.feeds = new ArrayList<Feed>(20);
            this.data.current = -2;
            getFragmentManager().beginTransaction().add(this.data, getString(R.string.drawer_retained_tag)).commit();
            reloadFeeds();
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_feeds, container);
        this.navList = (ListView) v.findViewById(R.id.drawer_left_nav_list);
        this.navAdapter = new FeedsAdapter(this.getActivity(), this.data.feeds);
        this.navList.setAdapter(navAdapter);

        this.progressBar = (ProgressBar) v.findViewById(R.id.drawer_left_nav_progressbar);
        this.navDrawer = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
        this.infoDrawerLayout = (RelativeLayout) getActivity().findViewById(R.id.drawer_right_info);

        this.navList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ((FeederActivity) getActivity()).openEntriesFragment(data.feeds.get(data.current));
            }
        });

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (getFragmentManager().findFragmentById(R.id.content_frame) instanceof StartFragment) {
            menu.findItem(R.id.action_display_short_feed_description).setVisible(false);

            if (this.navDrawer.isDrawerOpen(this.navDrawer))
                menu.findItem(R.id.action_refresh).setVisible(true);
            else
                menu.findItem(R.id.action_refresh).setVisible(false);
        }

        else {
            if (this.navDrawer.isDrawerOpen(this.navDrawer)) {
                menu.findItem(R.id.action_refresh).setVisible(true);
                menu.findItem(R.id.action_display_short_feed_description).setVisible(false);
            }
            else {
                menu.findItem(R.id.action_refresh).setVisible(true);
                menu.findItem(R.id.action_display_short_feed_description).setVisible(true);
            }
        }

    }

    @Override
    public void onDetach() {
        getActivity().setProgressBarIndeterminateVisibility(false);
        if (this.networkManager.isStarted())
            this.networkManager.shouldStop();
        super.onDetach();
    }

    /**
     * Reloads the list of the feeds.
     */
    void reloadFeeds() {
        getActivity().setProgressBarIndeterminateVisibility(true);
        if (!this.networkManager.isStarted())
            this.networkManager.start(getActivity());
        this.networkManager.execute(new JSONRequest<Feed[]>(Feed[].class, this.baseURL + getString(R.string.feeds_endpoint)), getString(R.string.categories_cache), DurationInMillis.ALWAYS_EXPIRED, new FeedsRequestListener());
    }

    private void showNavList() {
        this.progressBar.animate().alpha(0f)
                .setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime))
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        progressBar.setVisibility(View.GONE);
                    }
                });
        this.navList.animate().alpha(1f)
                .setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime))
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        navList.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void showListLoadFailScreen() {
        this.progressBar.animate().alpha(0f)
                .setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime))
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        progressBar.setVisibility(View.GONE);
                    }
                });
        this.navList.animate().alpha(1f)
                .setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime))
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        navList.setVisibility(View.VISIBLE);
                    }
                });
    }

    /* Object to listen for RoboSpice task completion. */
    private class FeedsRequestListener implements RequestListener<Feed[]> {
        @Override
        public void onRequestSuccess(Feed[] feeds) {
            getActivity().setProgressBarIndeterminateVisibility(false);
            data.feeds.clear();
            Collections.addAll(data.feeds, feeds);

            navAdapter.notifyDataSetChanged();
            showNavList();
            if (getFragmentManager().findFragmentById(R.id.content_frame) instanceof StartFragment && data.current < 0) {
                navDrawer.closeDrawer(infoDrawerLayout);
                navDrawer.openDrawer(navDrawer);
            }
        }

        @Override
        public void onRequestFailure(SpiceException e) {
            getActivity().setProgressBarIndeterminateVisibility(false);
            Log.d(getString(R.string.app_tag), "Feeds list load fail! " + e.getMessage() + "\n" + Arrays.toString(e.getStackTrace()));
            Toast.makeText(getActivity(), "Feed list load fail!", Toast.LENGTH_SHORT).show();
            showListLoadFailScreen();
        }
    }

    public static class DrawerDataFragment extends Fragment {
        public ArrayList<Feed> feeds;
        public int current;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
        }
    }
}
