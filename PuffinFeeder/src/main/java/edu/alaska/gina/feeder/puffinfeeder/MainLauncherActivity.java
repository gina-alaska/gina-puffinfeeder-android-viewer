package edu.alaska.gina.feeder.puffinfeeder;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.octo.android.robospice.*;
import com.octo.android.robospice.persistence.*;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import java.util.ArrayList;

/**
 * Class that handles navigation drawer and startup.
 * created by bobby on 6/14/13.
 */
public class MainLauncherActivity extends Activity {
    private static final String JSON_CACHE_KEY = "feeds_json_array";
    protected SpiceManager mSpiceManager = new SpiceManager(JsonSpiceService.class);

    protected ArrayList<String> listItems = new ArrayList<String>();
    protected ArrayAdapter<String> primary;
    protected Feed[] masterFeedsList;
    protected int current = -2;

    protected Menu aBarMenu;

    protected DrawerLayout mDrawerLayout; //Contains the entire activity.
    protected ListView mDrawerList; //ListView of Nav Drawer.
    protected ActionBarDrawerToggle mDrawerToggle; //Indicates presence of nav drawer in action bar.

    /** Overridden Methods */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.main_activity_launcher);
        setProgressBarIndeterminateVisibility(false);

        if (savedInstanceState != null)
            current = savedInstanceState.getInt("current");

        if (current < 0) {
            StartFragment sFrag = new StartFragment();
            getFragmentManager().beginTransaction().replace(R.id.content_frame, sFrag, "start").commit();
        }
        else {
            ImageFeedFragment iFrag = new ImageFeedFragment();
            Bundle arggh = new Bundle();

            arggh.putInt("position", current);
            arggh.putString("title", masterFeedsList[current].getTitle());
            arggh.putString("entries", masterFeedsList[current].getEntries());
            arggh.putString("slug", masterFeedsList[current].getSlug());
            arggh.putBoolean("status", masterFeedsList[current].getStatus());
            arggh.putString("description", masterFeedsList[current].getDescription());
            arggh.putString("info", masterFeedsList[current].getMoreinfo());

            iFrag.setArguments(arggh);
            getFragmentManager().beginTransaction().replace(R.id.content_frame, iFrag).addToBackStack(null).commit();
        }

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.drawer_List);

        getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE, ActionBar.DISPLAY_SHOW_CUSTOM);
        listItems.add("Nothing to see.");

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerClosed(View drawerView) {
                if (getFragmentManager().findFragmentById(R.id.content_frame) instanceof StartFragment)
                    getActionBar().setTitle("GINA Puffin Feeder");
                else
                    getActionBar().setTitle(masterFeedsList[current].getTitle());
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle("Select a Feed");
                invalidateOptionsMenu();
            }
        };

        primary = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItems);

        mDrawerList.setAdapter(primary);
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (current < 0)
            refreshFeedsList(DurationInMillis.ONE_DAY);

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!listItems.get(position).equals("Nothing to see.")) {
                    current = position;
                    ImageFeedFragment iFrag = new ImageFeedFragment();
                    Bundle intel = new Bundle();

                    intel.putInt("position", position);
                    intel.putString("title", masterFeedsList[position].getTitle());
                    intel.putString("entries", masterFeedsList[position].getEntries());
                    intel.putString("slug", masterFeedsList[position].getSlug());
                    intel.putBoolean("status", masterFeedsList[position].getStatus());
                    intel.putString("description", masterFeedsList[current].getDescription());
                    intel.putString("info", masterFeedsList[current].getMoreinfo());

                    iFrag.setArguments(intel);
                    getFragmentManager().beginTransaction().replace(R.id.content_frame, iFrag, "grid").addToBackStack(null).commit();
                }
                mDrawerLayout.closeDrawer(mDrawerList);
            }
        });

        primary.notifyDataSetChanged();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("current", current);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onPause() {
        setProgressBarIndeterminateVisibility(false);
        if (mSpiceManager.isStarted())
            mSpiceManager.shouldStop();
        super.onPause();
    }

    @Override
    protected void onStop() {
        setProgressBarIndeterminateVisibility(false);
        if (mSpiceManager.isStarted())
            mSpiceManager.shouldStop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        setProgressBarIndeterminateVisibility(false);
        if (mSpiceManager.isStarted())
            mSpiceManager.shouldStop();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setProgressBarIndeterminateVisibility(false);
        try {
            if (getFragmentManager().findFragmentById(R.id.content_frame) instanceof StartFragment) {
                aBarMenu.findItem(R.id.action_refresh).setVisible(false);
                aBarMenu.findItem(R.id.action_load_next).setVisible(false);
                aBarMenu.findItem(R.id.action_load_prev).setVisible(false);
                aBarMenu.findItem(R.id.action_display_short_description).setVisible(false);
                getActionBar().setTitle("GINA Puffin Feeder");
            }
        } catch (NullPointerException e) {/*Do Nothing*/}
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (getFragmentManager().findFragmentById(R.id.content_frame) instanceof StartFragment) {
            menu.findItem(R.id.action_load_next).setVisible(false);
            menu.findItem(R.id.action_load_prev).setVisible(false);
            menu.findItem(R.id.action_load_first).setVisible(false);
            menu.findItem(R.id.action_display_short_feed_description).setVisible(false);

            if (mDrawerLayout.isDrawerOpen(mDrawerList))
                menu.findItem(R.id.action_refresh).setVisible(true);
            else
                menu.findItem(R.id.action_refresh).setVisible(false);
        }

        else {
            if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
                menu.findItem(R.id.action_refresh).setVisible(true);
                menu.findItem(R.id.action_load_next).setVisible(false);
                menu.findItem(R.id.action_load_prev).setVisible(false);
                menu.findItem(R.id.action_load_first).setVisible(false);
                menu.findItem(R.id.action_display_short_feed_description).setVisible(false);
            }
            else {
                menu.findItem(R.id.action_refresh).setVisible(true);
                menu.findItem(R.id.action_load_next).setVisible(true);
                menu.findItem(R.id.action_load_prev).setVisible(true);
                menu.findItem(R.id.action_load_first).setVisible(true);
                menu.findItem(R.id.action_display_short_feed_description).setVisible(true);
            }
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.launcher, menu);
        aBarMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (mDrawerLayout.isDrawerOpen(mDrawerList))
                    mDrawerLayout.closeDrawer(mDrawerList);
                else
                    mDrawerLayout.openDrawer(mDrawerList);
                return true;

            case R.id.action_refresh:
                if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
                    refreshFeedsList(DurationInMillis.ALWAYS_EXPIRED);
                    primary.notifyDataSetChanged();
                    return true;
                }
                break;

            case R.id.action_display_short_feed_description:
                Bundle info = new Bundle();
                info.putString("description", masterFeedsList[current].getDescription());
                info.putString("title", masterFeedsList[current].getTitle());
                info.putString("url", masterFeedsList[current].getMoreinfo());

                ShortDescriptionFragment dFrag = new ShortDescriptionFragment();
                dFrag.setArguments(info);

                dFrag.show(getFragmentManager(), "description_dialog");

                return true;

            case R.id.action_show_credits:
                this.startActivity(new Intent(this, WebViewActivity.class).putExtra("url", getString(R.string.credits_filepath)).putExtra("title", getString(R.string.credits_title)));
                return true;

            case R.id.action_open_preferences:
                this.startActivity(new Intent(this, PreferencesActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /** Class to run after RoboSpice task completion. */
    private class FeedsRequestListener implements RequestListener<Feed[]> {
        @Override
        public void onRequestSuccess(Feed[] feed) {
            setProgressBarIndeterminateVisibility(false);
            masterFeedsList = feed;

            listItems.clear();
            for (Feed f : feed)
                listItems.add(f.getTitle());

            Activity a = new Activity();
            a.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    primary.notifyDataSetChanged();
                    if (getFragmentManager().findFragmentById(R.id.content_frame) instanceof StartFragment && current < 0)
                        mDrawerLayout.openDrawer(mDrawerList);
                }
            });

            if (isOnline())
                Toast.makeText(getApplicationContext(), "Feed list reloaded.", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(getApplicationContext(), "Feed list reloaded from cache. Please check internet connection.", Toast.LENGTH_LONG).show();

            if (mSpiceManager.isStarted())
                mSpiceManager.shouldStop();
        }

        @Override
        public void onRequestFailure(SpiceException e) {
            setProgressBarIndeterminateVisibility(false);
            Log.d(getString(R.string.app_tag), "Feeds list load fail! " + e.getMessage() + "\n" + e.getStackTrace());
            Toast.makeText(getApplicationContext(), "Feed list load fail!", Toast.LENGTH_SHORT).show();
            if (mSpiceManager.isStarted())
                mSpiceManager.shouldStop();
        }
    }

    /**
     * Reloads the list of the feeds.
     * @param expiration_time Time if its been at least this long since last update, do it.
     */
    public void refreshFeedsList(long expiration_time) {
        setProgressBarIndeterminateVisibility(true);
        if (!mSpiceManager.isStarted())
            mSpiceManager.start(this.getBaseContext());
        mSpiceManager.execute(new FeedsJsonRequest(), JSON_CACHE_KEY, expiration_time, new FeedsRequestListener());
    }

    /**
     * Returns whether the device is actively connected to a network.
     * @return "true" if yes, "false" otherwise.
     */
    public boolean isOnline() {
        ConnectivityManager cm10_1 = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
        NetworkInfo nFo = cm10_1.getActiveNetworkInfo();
        if (nFo != null && nFo.isConnectedOrConnecting())
            return true;
        return false;
    }
}
