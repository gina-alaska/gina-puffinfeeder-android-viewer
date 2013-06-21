package edu.alaska.gina.feeder.puffinfeeder;

import android.app.ActionBar;
import android.app.Activity;
import android.content.res.Configuration;
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
    protected MenuItem mMenuItem;
    protected int current = -1;

    protected DrawerLayout mDrawerLayout; //Contains the entire activity.
    protected ListView mDrawerList; //ListView of Nav Drawer.
    protected ActionBarDrawerToggle mDrawerToggle; //Indicates presence of nav drawer in action bar.
    protected String mTitle; //Title of Action Bar.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity_launcher);

        StartFragment sFrag = new StartFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, sFrag).addToBackStack(null).commit();

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mMenuItem = (MenuItem) findViewById(R.id.action_refresh);
        mDrawerList = (ListView) findViewById(R.id.drawer_List);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE, ActionBar.DISPLAY_SHOW_CUSTOM);
        listItems.add("Nothing to see.");

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerClosed(View drawerView) {
                if (current < 0)
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
                getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, iFrag).addToBackStack(null).commit();

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

    public void refreshFeedsList(long expiration_time) {
        //mMenuItem.setActionView(R.layout.actionbar_progress_bar);
        //mMenuItem.expandActionView();

        mSpiceManager.execute(new FeedsJsonRequest(), JSON_CACHE_KEY, expiration_time, new FeedsRequestListener());
    }

    @Override
    protected void onStop() {
        mSpiceManager.shouldStop();
        super.onStop();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mDrawerLayout.isDrawerOpen(mDrawerList))
            menu.findItem(R.id.action_refresh).setVisible(true);
        else
            menu.findItem(R.id.action_refresh).setVisible(false);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.launcher, menu);
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
                //mMenuItem = (MenuItem) findViewById(R.id.action_refresh);
                //mMenuItem.setActionView(R.layout.actionbar_progress_bar);
                //mMenuItem.expandActionView();
                if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
                    refreshFeedsList(DurationInMillis.ALWAYS_EXPIRED);
                    primary.notifyDataSetChanged();
                    return true;
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private class FeedsRequestListener implements RequestListener<Feed[]> {
        @Override
        public void onRequestSuccess(Feed[] feed) {
            //mMenuItem.collapseActionView();
            //mMenuItem.setActionView(null);

            masterFeedsList = feed;

            listItems.clear();
            for (Feed f : feed)
                listItems.add(f.getTitle());

            Activity a = new Activity();
            a.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    primary.notifyDataSetChanged();
                }
            });

            Toast.makeText(getApplicationContext(), "Feed list reloaded.", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onRequestFailure(SpiceException e) {
            Log.d("Feeder Viewer", "Feeds list load fail! " + e.getMessage() + "\n" + e.getStackTrace());
            Toast.makeText(getApplicationContext(), "Feed list load fail!", Toast.LENGTH_SHORT).show();
        }
    }
}
