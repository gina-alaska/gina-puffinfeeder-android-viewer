package edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
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
    private EntriesFragment contentFragment;

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
        if (getActionBar() != null)
            getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE, ActionBar.DISPLAY_SHOW_CUSTOM);

        this.mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        this.navDrawerList = (FrameLayout) findViewById(R.id.drawer_left_nav);
        this.infoDrawerLayout = (RelativeLayout) findViewById(R.id.drawer_right_info);

        this.feedsDrawer = new FeedsFragment();
        this.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, this.infoDrawerLayout);

        this.mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (drawerView == navDrawerList) {
                    if (getFragmentManager().findFragmentById(R.id.content_frame) instanceof StartFragment) {
                        getActionBar().setTitle("GINA Puffin Feeder");
                    } else {
                        if (contentFragment == null)
                            contentFragment = ((EntriesFragment) getFragmentManager().findFragmentById(R.id.content_frame));
                        getActionBar().setTitle(contentFragment.getCurrentFeed().title);
                    }
                    invalidateOptionsMenu();
                } else {
                    mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, infoDrawerLayout);
                }
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (drawerView == navDrawerList) {
                    if (mDrawerLayout.isDrawerOpen(navDrawerList)) {
                        getActionBar().setTitle("GINA Puffin Feeder");
                        if (mDrawerLayout.isDrawerOpen(infoDrawerLayout))
                            mDrawerLayout.closeDrawer(infoDrawerLayout);
                    }
                    invalidateOptionsMenu();
                } else {
                    mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, infoDrawerLayout);
                }
            }
        };
        this.mDrawerLayout.setDrawerListener(mDrawerToggle);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        getFragmentManager().beginTransaction().replace(R.id.drawer_left_nav, this.feedsDrawer, "nav_drawer").commit();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        this.mDrawerToggle.syncState();
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

        if (mDrawerLayout.isDrawerOpen(Gravity.END)) {
            mDrawerLayout.closeDrawer(Gravity.END);
            return;
        } else if (mDrawerLayout.isDrawerOpen(Gravity.START)) {
            mDrawerLayout.closeDrawer(Gravity.START);
            return;
        }

        if (getFragmentManager().findFragmentById(R.id.content_frame) instanceof StartFragment) {
            ((TextView) findViewById(R.id.description_body)).setText(getResources().getString(R.string.description_placeholder));

            aBarMenu.findItem(R.id.action_refresh).setVisible(false);
            aBarMenu.findItem(R.id.action_display_short_feed_description).setVisible(false);

            if (getActionBar() != null)
                getActionBar().setTitle("GINA Puffin Feeder");
        } else if (getFragmentManager().findFragmentById(R.id.content_frame) instanceof EntriesFragment) {
            this.contentFragment = (EntriesFragment) getFragmentManager().findFragmentById(R.id.content_frame);
            getActionBar().setTitle(this.contentFragment.getCurrentFeed().title);
        }
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
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            if (mDrawerLayout.isDrawerOpen(this.infoDrawerLayout))
                this.mDrawerLayout.closeDrawer(infoDrawerLayout);
            return true;
        }

        switch (item.getItemId()) {
            case R.id.action_refresh:
                if (mDrawerLayout.isDrawerOpen(navDrawerList)) {
                    this.feedsDrawer.reloadFeeds();
                    return true;
                }
                break;

            case R.id.action_display_short_feed_description:
                if (contentFragment == null)
                    contentFragment = ((EntriesFragment) getFragmentManager().findFragmentById(R.id.content_frame));
                setDescription(this.contentFragment.getCurrentFeed());
                mDrawerLayout.openDrawer(infoDrawerLayout);
                return true;

            case R.id.action_show_credits:
                this.startActivity(new Intent(this, WebViewActivity.class).putExtra("url", getString(R.string.credits_filepath)).putExtra("title", getString(R.string.credits_title)));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setDescription(Feed newFeed) {
        ((TextView) infoDrawerLayout.findViewById(R.id.description_body)).setText(newFeed.description);
        if (newFeed.more_info_url == null)
            infoDrawerLayout.findViewById(R.id.more_info_button).setVisibility(View.GONE);
        else
            infoDrawerLayout.findViewById(R.id.more_info_button).setVisibility(View.VISIBLE);
    }

    @Override
    public void openEntriesFragment(Feed newFeed) {
        this.contentFragment = new EntriesFragment();
        Bundle b = new Bundle();
        b.putSerializable("feed", newFeed);
        contentFragment.setArguments(b);
        getFragmentManager().beginTransaction().replace(R.id.content_frame, contentFragment, "entries").addToBackStack(null).commit();
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
