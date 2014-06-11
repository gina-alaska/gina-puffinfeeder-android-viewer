package edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer;

import android.app.*;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.*;
import android.widget.*;

import com.octo.android.robospice.*;
import com.octo.android.robospice.persistence.*;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer.network.JsonSpiceService;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Class that handles navigation drawer and startup.
 * created by bobby on 6/14/13.
 */
public class MainLauncherActivity extends Activity {
    private static final String JSON_CACHE_KEY = "feeds_json_array";
    private final SpiceManager mSpiceManager = new SpiceManager(JsonSpiceService.class);
    private final FeedsJsonRequest mSpiceRequest = new FeedsJsonRequest();

    private final ArrayList<String> listItems = new ArrayList<String>();
    private ArrayAdapter<String> primary;
    private Feed[] masterFeedsList;
    private int current = -2;

    private Menu aBarMenu;

    private DrawerLayout mDrawerLayout; //Contains the entire activity.
    private ListView navDrawerList; //ListView of Nav Drawer.
    private LinearLayout infoDrawerLayout; //Layout for the Info Drawer.
    private ActionBarDrawerToggle mDrawerToggle; //Indicates presence of nav drawer in action bar.

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
            openPreviewFragment(current);
        }

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navDrawerList = (ListView) findViewById(R.id.drawer_left_nav);
        infoDrawerLayout = (LinearLayout) findViewById(R.id.drawer_right_info);

        if (getActionBar() != null)
            getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE, ActionBar.DISPLAY_SHOW_CUSTOM);
        listItems.add("No Feeds Loaded.");

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

        navDrawerList.setAdapter(primary);
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

        navDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!listItems.get(position).equals("No Feeds Loaded.")) {
                    openPreviewFragment(position);
                }
                mDrawerLayout.closeDrawer(navDrawerList);
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
                ((TextView) findViewById(R.id.description)).setText(getResources().getString(R.string.description_placeholder));

                aBarMenu.findItem(R.id.action_refresh).setVisible(false);
                aBarMenu.findItem(R.id.action_load_next).setVisible(false);
                aBarMenu.findItem(R.id.action_load_prev).setVisible(false);
                aBarMenu.findItem(R.id.action_display_short_description).setVisible(false);

                if (getActionBar() != null)
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

            if (mDrawerLayout.isDrawerOpen(navDrawerList))
                menu.findItem(R.id.action_refresh).setVisible(true);
            else
                menu.findItem(R.id.action_refresh).setVisible(false);
        }

        else {
            if (mDrawerLayout.isDrawerOpen(navDrawerList)) {
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

        if (getResources().getString(R.string.alpha_test_build).equals("yes"))
            menu.findItem(R.id.action_change_base_url).setVisible(true);
        else
            menu.findItem(R.id.action_change_base_url).setVisible(false);

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
                mDrawerLayout.closeDrawer(infoDrawerLayout);
                if (mDrawerLayout.isDrawerOpen(navDrawerList))
                    mDrawerLayout.closeDrawer(navDrawerList);
                else
                    mDrawerLayout.openDrawer(navDrawerList);
                return true;

            case R.id.action_refresh:
                if (mDrawerLayout.isDrawerOpen(navDrawerList)) {
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

            case R.id.action_change_base_url:
                new URLChanger().show(getFragmentManager(), "change_url");
                break;
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
                        mDrawerLayout.openDrawer(navDrawerList);
                }
            });

            if (isOnline())
                Toast.makeText(getApplicationContext(), "Feed list reloaded.", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(getApplicationContext(), "Feed list reloaded from cache. Please check internet connection.", Toast.LENGTH_LONG).show();
/*
            if (mSpiceManager.isStarted())
                mSpiceManager.shouldStop(); */
        }

        @Override
        public void onRequestFailure(SpiceException e) {
            setProgressBarIndeterminateVisibility(false);
            Log.d(getString(R.string.app_tag), "Feeds list load fail! " + e.getMessage() + "\n" + Arrays.toString(e.getStackTrace()));
            Toast.makeText(getApplicationContext(), "Feed list load fail!", Toast.LENGTH_SHORT).show();
        }
    }

    private class URLChanger extends DialogFragment {
        View v;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            v = getLayoutInflater().inflate(R.layout.dialog_change_url, null);
            AlertDialog.Builder b = new AlertDialog.Builder(this.getActivity());
            b.setView(v).setTitle("Enter Base URL").setNeutralButton("Dismiss", null).setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String s = ((EditText) v.findViewById(R.id.newBaseURL)).getText().toString();
                    if (!s.equals(""))
                        mSpiceRequest.baseURL = s;
                    mSpiceManager.removeAllDataFromCache();
                    listItems.clear();
                    refreshFeedsList(DurationInMillis.ALWAYS_EXPIRED);
                }
            });

            return b.create();
        }
    }

    /**
     * Method that loads a feed into a fragment.
     * @param position Index of feed to be loaded.
     */
    private void openPreviewFragment(int position) {
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

        ((TextView) findViewById(R.id.description)).setText(masterFeedsList[current].getDescription());
    }

    /**
     * Reloads the list of the feeds.
     * @param expiration_time Time if its been at least this long since last update, do it.
     */
    void refreshFeedsList(long expiration_time) {
        setProgressBarIndeterminateVisibility(true);
        if (!mSpiceManager.isStarted())
            mSpiceManager.start(this.getBaseContext());
        mSpiceManager.execute(mSpiceRequest, JSON_CACHE_KEY, expiration_time, new FeedsRequestListener());
    }

    /**
     * Returns whether the device is actively connected to a network.
     * @return "true" if yes, "false" otherwise.
     */
    boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo nFo = cm.getActiveNetworkInfo();
        return (nFo != null && nFo.isConnectedOrConnecting());
    }
}
