package edu.alaska.gina.feeder.puffinfeeder;

import android.app.ActionBar;
import android.app.Activity;
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
import com.actionbarsherlock.internal.view.menu.MenuWrapper;
import com.actionbarsherlock.view.*;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.octo.android.robospice.*;
import com.octo.android.robospice.persistence.*;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import java.util.ArrayList;

public class MainLauncherActivity extends SherlockActivity {
    private static final String JSON_CACHE_KEY = "feeds_json_array";
    protected SpiceManager mSpiceManager = new SpiceManager(JsonSpiceService.class);
    protected ArrayList<String> listItems = new ArrayList<String>();
    protected ArrayAdapter<String> primary;
    protected Feed[] masterFeedsList;
    protected MenuItem mMenuItem;

    DrawerLayout mDrawerLayout; //Contains the entire activity.
    ListView mDrawerList; //ListView of Nav Drawer.
    ActionBarDrawerToggle mDrawerToggle; //Indicates presence of nav drawer in action bar.
    String mTitle = null; //Title of Action Bar.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity_launcher);

        mTitle = (String) getTitle();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mMenuItem = (MenuItem) findViewById(R.id.action_refresh);
        mDrawerList = (ListView) findViewById(R.id.drawer_List);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_CUSTOM);
        listItems.add("Nothing to see.");

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerClosed(View drawerView) {
                getSupportActionBar().setTitle(mTitle);
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
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mSpiceManager.start(this);

        refreshFeedsList(DurationInMillis.ONE_DAY);

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mTitle = masterFeedsList[position].getTitle();
                ImageFeedFragment iFrag = new ImageFeedFragment();
                Bundle intel = new Bundle();

                intel.putInt("position", position);
                intel.putString("title", mTitle);
                intel.putString("entries", masterFeedsList[position].getEntries());
                intel.putBoolean("status", masterFeedsList[position].getStatus());

                iFrag.setArguments(intel);
                getFragmentManager().beginTransaction().replace(R.id.content_frame, iFrag).addToBackStack(null).commit();

                mDrawerLayout.closeDrawer(mDrawerList);
            }
        });

        primary.notifyDataSetChanged();
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
        menu.findItem(R.id.action_refresh).setVisible(true);
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
            case R.id.action_refresh:
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
        }

        @Override
        public void onRequestFailure(SpiceException e) {
            Log.d("Feeder Viewer", "Feeds list load fail! " + e.getMessage());
            Toast.makeText(getApplicationContext(), "Feed list load fail!", 5000).show();
        }
    }
}
