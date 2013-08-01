package edu.alaska.gina.feeder.puffinfeeder;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.*;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.octo.android.robospice.*;
import com.octo.android.robospice.persistence.*;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import java.util.ArrayList;

/**
 * Class that handles navigation drawer and startup.
 * created by bobby on 6/14/13.
 */
public class MainLauncherActivity extends SherlockFragmentActivity {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.main_activity_launcher);

        if (savedInstanceState != null)
            current = savedInstanceState.getInt("current");

        if (current < 0) {
            StartFragment sFrag = new StartFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, sFrag, "start").commit();
        }
        else {
            ImageFeedFragment iFrag = new ImageFeedFragment();
            Bundle arggh = new Bundle();

            arggh.putInt("position", current);
            arggh.putString("title", masterFeedsList[current].getTitle());
            arggh.putString("entries", masterFeedsList[current].getEntries());
            arggh.putString("slug", masterFeedsList[current].getSlug());
            arggh.putBoolean("status", masterFeedsList[current].getStatus());

            iFrag.setArguments(arggh);
            getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, iFrag).addToBackStack(null).commit();
        }

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.drawer_List);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE, ActionBar.DISPLAY_SHOW_CUSTOM);
        listItems.add("Nothing to see.");

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerClosed(View drawerView) {
                if (getSupportFragmentManager().findFragmentById(R.id.content_frame) instanceof StartFragment)
                    getSupportActionBar().setTitle("GINA Puffin Feeder");
                else
                    getSupportActionBar().setTitle(masterFeedsList[current].getTitle());
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().setTitle("Select a Feed");
                invalidateOptionsMenu();
            }
        };

        primary = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItems);

        mDrawerList.setAdapter(primary);
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mSpiceManager.start(this.getBaseContext());

        if (current < 0)
            refreshFeedsList(DurationInMillis.ONE_DAY);

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                current = position;
                ImageFeedFragment iFrag = new ImageFeedFragment();
                Bundle intel = new Bundle();

                intel.putInt("position", position);
                intel.putString("title", masterFeedsList[position].getTitle());
                intel.putString("entries", masterFeedsList[position].getEntries());
                intel.putString("slug", masterFeedsList[position].getSlug());
                intel.putBoolean("status", masterFeedsList[position].getStatus());

                iFrag.setArguments(intel);
                getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, iFrag, "grid").addToBackStack(null).commit();

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

    public void refreshFeedsList(long expiration_time) {
        mSpiceManager.execute(new FeedsJsonRequest(), JSON_CACHE_KEY, expiration_time, new FeedsRequestListener());
    }

    @Override
    protected void onStop() {
        mSpiceManager.shouldStop();
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().findFragmentById(R.id.content_frame) instanceof StartFragment) {
            aBarMenu.findItem(R.id.action_refresh).setVisible(false);
            aBarMenu.findItem(R.id.action_load_next).setVisible(false);
            aBarMenu.findItem(R.id.action_load_prev).setVisible(false);
            aBarMenu.findItem(R.id.action_display_short_description).setVisible(false);
            getSupportActionBar().setTitle("GINA Puffin Feeder");
        }
        super.onBackPressed();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (getSupportFragmentManager().findFragmentById(R.id.content_frame) instanceof StartFragment) {
            menu.findItem(R.id.action_load_next).setVisible(false);
            menu.findItem(R.id.action_load_prev).setVisible(false);
            menu.findItem(R.id.action_load_first).setVisible(false);
            menu.findItem(R.id.action_display_short_description).setVisible(false);

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
                menu.findItem(R.id.action_display_short_description).setVisible(false);
            }
            else {
                menu.findItem(R.id.action_refresh).setVisible(true);
                menu.findItem(R.id.action_load_next).setVisible(true);
                menu.findItem(R.id.action_load_prev).setVisible(true);
                menu.findItem(R.id.action_load_first).setVisible(true);
                menu.findItem(R.id.action_display_short_description).setVisible(true);
            }
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.launcher, menu);
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

            case R.id.action_display_short_description:
                Bundle info = new Bundle();
                info.putString("description", masterFeedsList[current].getDescription());
                info.putString("title", masterFeedsList[current].getTitle());

                ShortDescriptionFragment dFrag = new ShortDescriptionFragment();
                dFrag.setArguments(info);

                dFrag.show(getFragmentManager(), "description_dialog");

                return true;
            case R.id.action_show_credits:
                this.startActivity(new Intent(this, WebViewActivity.class));
                return true;
            case R.id.action_open_preferences:
                this.startActivity(new Intent(this, PreferencesActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean isOnline() {
        ConnectivityManager cm10_1 = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
        NetworkInfo nFo = cm10_1.getActiveNetworkInfo();
        if (nFo != null && nFo.isConnectedOrConnecting())
            return true;
        return false;
    }

    private class FeedsRequestListener implements RequestListener<Feed[]> {
        @Override
        public void onRequestSuccess(Feed[] feed) {
            masterFeedsList = feed;

            listItems.clear();
            for (Feed f : feed)
                listItems.add(f.getTitle());

            Activity a = new Activity();
            a.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    primary.notifyDataSetChanged();
                    if (getSupportFragmentManager().findFragmentById(R.id.content_frame) instanceof StartFragment && current < 0)
                        mDrawerLayout.openDrawer(mDrawerList);
                }
            });

            if (isOnline())
                Toast.makeText(getApplicationContext(), "Feed list reloaded.", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(getApplicationContext(), "Feed list reloaded from cache. Please check internet connection.", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onRequestFailure(SpiceException e) {
            Log.d(getString(R.string.app_tag), "Feeds list load fail! " + e.getMessage() + "\n" + e.getStackTrace());
            Toast.makeText(getApplicationContext(), "Feed list load fail!", Toast.LENGTH_SHORT).show();
        }
    }
}
