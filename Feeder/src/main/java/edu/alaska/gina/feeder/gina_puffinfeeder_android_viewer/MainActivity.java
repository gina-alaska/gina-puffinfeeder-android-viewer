package edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import edu.alaska.gina.feeder.android.core.data.Feed;
import edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer.fragments.EntriesFragment;
import edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer.fragments.FeederActivity;
import edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer.fragments.FeedsFragment;
import edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer.fragments.StartFragment;

/**
 * Activity that handles navigation drawer and startup.
 * created by bobby on 6/14/13.
 */
public class MainActivity extends Activity implements FeederActivity {
    private FeedsFragment feedsDrawer;
    private Feed currentFeed;

    private Menu aBarMenu;
    private DrawerLayout mDrawerLayout; //Contains the entire activity.
    private FrameLayout navDrawerList; //View contaning Nav Drawer.
    private RelativeLayout infoDrawerLayout; //Layout for the Info Drawer.
    private ActionBarDrawerToggle mDrawerToggle; //Indicates presence of nav drawer in action bar.

    /* Overridden Methods */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_main);
        setProgressBarIndeterminateVisibility(false);

        this.mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        this.navDrawerList = (FrameLayout) findViewById(R.id.drawer_left_nav);
        this.infoDrawerLayout = (RelativeLayout) findViewById(R.id.drawer_right_info);

        this.feedsDrawer = new FeedsFragment();
        this.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, this.infoDrawerLayout);

        if (getActionBar() != null) {
            getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE, ActionBar.DISPLAY_SHOW_CUSTOM);
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setHomeButtonEnabled(true);
        }

        this.mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerClosed(View drawerView) {
                if (getFragmentManager().findFragmentById(R.id.content_frame) instanceof StartFragment)
                    getActionBar().setTitle("GINA Puffin Feeder");
                else
                    getActionBar().setTitle(currentFeed.title);
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                if (mDrawerLayout.isDrawerOpen(navDrawerList)) {
                    getActionBar().setTitle("GINA Puffin Feeder");
                    invalidateOptionsMenu();
                }
            }
        };

        getFragmentManager().beginTransaction().replace(R.id.drawer_left_nav, this.feedsDrawer, "nav_drawer").commit();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onDestroy() {
        setProgressBarIndeterminateVisibility(false);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setProgressBarIndeterminateVisibility(false);

        if (mDrawerLayout.isDrawerOpen(infoDrawerLayout)) {
            mDrawerLayout.closeDrawer(infoDrawerLayout);
            return;
        }

        try {
            if (getFragmentManager().findFragmentById(R.id.content_frame) instanceof StartFragment) {
                ((TextView) findViewById(R.id.description_body)).setText(getResources().getString(R.string.description_placeholder));

                aBarMenu.findItem(R.id.action_refresh).setVisible(false);
                aBarMenu.findItem(R.id.action_display_short_description).setVisible(false);

                if (getActionBar() != null)
                    getActionBar().setTitle("GINA Puffin Feeder");
            }
        } catch (NullPointerException e) {/*Do Nothing*/}
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (getFragmentManager().findFragmentById(R.id.content_frame) instanceof StartFragment) {
            menu.findItem(R.id.action_display_short_feed_description).setVisible(false);

            if (mDrawerLayout.isDrawerOpen(navDrawerList))
                menu.findItem(R.id.action_refresh).setVisible(true);
            else
                menu.findItem(R.id.action_refresh).setVisible(false);
        }

        else {
            if (mDrawerLayout.isDrawerOpen(navDrawerList)) {
                menu.findItem(R.id.action_refresh).setVisible(true);
                menu.findItem(R.id.action_display_short_feed_description).setVisible(false);
            }
            else {
                menu.findItem(R.id.action_refresh).setVisible(true);
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
                mDrawerLayout.closeDrawer(infoDrawerLayout);
                if (mDrawerLayout.isDrawerOpen(navDrawerList))
                    mDrawerLayout.closeDrawer(navDrawerList);
                else
                    mDrawerLayout.openDrawer(navDrawerList);
                return true;

            case R.id.action_refresh:
                if (mDrawerLayout.isDrawerOpen(navDrawerList)) {
                    this.feedsDrawer.reloadFeeds();
                    return true;
                }
                break;

            case R.id.action_display_short_feed_description:
                mDrawerLayout.openDrawer(infoDrawerLayout);
                return true;

            case R.id.action_show_credits:
                this.startActivity(new Intent(this, WebViewActivity.class).putExtra("url", getString(R.string.credits_filepath)).putExtra("title", getString(R.string.credits_title)));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Method that loads a new EntriesFragment into the main content view.
     * @param newFeed New feed to be loaded.
     */
    @Override
    public void openEntriesFragment(Feed newFeed) {
        this.currentFeed = newFeed;
        EntriesFragment iFrag = new EntriesFragment();

        if (getActionBar() != null) {
            getActionBar().setTitle(newFeed.title);
        }

        Bundle b = new Bundle();
        b.putString("entries", newFeed.entries_url);
        iFrag.setArguments(b);
        getFragmentManager().beginTransaction().replace(R.id.content_frame, iFrag, "grid").addToBackStack(null).commit();
    }

    @Override
    public void openNavDrawer() {
        mDrawerLayout.closeDrawer(infoDrawerLayout);
        mDrawerLayout.openDrawer(navDrawerList);
    }

    @Override
    public void closeNavDrawer() {
        mDrawerLayout.closeDrawer(navDrawerList);
    }
}
