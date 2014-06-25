package edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer;

import android.app.*;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
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
import edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer.data.*;
import edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer.data.Feed;
import edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer.network.CategoriesRequest;
import edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer.network.JsonSpiceService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * Class that handles navigation drawer and startup.
 * created by bobby on 6/14/13.
 */
public class MainLauncherActivity extends Activity {
    private final SpiceManager mSpiceManager = new SpiceManager(JsonSpiceService.class);
    private CategoriesRequest mSpiceRequest = new CategoriesRequest();

    private final ArrayList<String> listItems = new ArrayList<String>();
    private ArrayAdapter<String> primary;
    private ArrayList<Category> masterFeedsList;
    private int current = -2;

    private Menu aBarMenu;
    private DataFragment retained;

    private DrawerLayout mDrawerLayout; //Contains the entire activity.
    private ListView navDrawerList; //ListView of Nav Drawer.
    private RelativeLayout infoDrawerLayout; //Layout for the Info Drawer.
    private ActionBarDrawerToggle mDrawerToggle; //Indicates presence of nav drawer in action bar.

    /** Overridden Methods */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.main_activity_launcher);
        setProgressBarIndeterminateVisibility(false);
        mSpiceRequest.setUrl(getString(R.string.base_url) + getString(R.string.categories_endpoint));
        masterFeedsList = new ArrayList<Category>();

        retained = (DataFragment) getFragmentManager().findFragmentByTag("data");

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navDrawerList = (ListView) findViewById(R.id.drawer_left_nav);
        infoDrawerLayout = (RelativeLayout) findViewById(R.id.drawer_right_info);

        if (getActionBar() != null)
            getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE, ActionBar.DISPLAY_SHOW_CUSTOM);
        listItems.add("No Feeds Loaded.");

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerClosed(View drawerView) {
                if (getFragmentManager().findFragmentById(R.id.content_frame) instanceof StartFragment)
                    getActionBar().setTitle("GINA Puffin Feeder");
                else
                    getActionBar().setTitle(masterFeedsList.get(0).feeds.get(current).title);
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                if (mDrawerLayout.isDrawerOpen(navDrawerList)) {
                    getActionBar().setTitle("Select a Feed");
                    invalidateOptionsMenu();
                }
            }
        };

        primary = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItems);

        navDrawerList.setAdapter(primary);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, infoDrawerLayout);
        mDrawerLayout.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && mDrawerLayout.isDrawerOpen(infoDrawerLayout)) {
                    mDrawerLayout.closeDrawer(infoDrawerLayout);
                    return true;
                }
                return false;
            }
        });

        findViewById(R.id.more_info_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(masterFeedsList.get(0).feeds.get(current).more_info_url));
                startActivity(browserIntent);
            }
        });

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

        primary.notifyDataSetChanged();

        if (retained == null) {
            retained = new DataFragment();
            getFragmentManager().beginTransaction().add(retained, "data").commit();
            refreshFeedsList(DurationInMillis.ALWAYS_EXPIRED);
        } else {
            masterFeedsList = retained.saveList;
            current = retained.current;
            updateDrawer(masterFeedsList);
        }

        if (current < 0) {
            StartFragment sFrag = new StartFragment();
            getFragmentManager().beginTransaction().replace(R.id.content_frame, sFrag, "start").commit();
            findViewById(R.id.more_info_button).setVisibility(View.GONE);
        } else {
            openPreviewFragment(current);
        }

        navDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!listItems.get(position).equals("No Feeds Loaded.")) {
                    openPreviewFragment(position);
                }
                mDrawerLayout.closeDrawer(navDrawerList);
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onPause() {
        setProgressBarIndeterminateVisibility(false);
        retained.saveList = this.masterFeedsList;
        retained.current = this.current;
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

        if (mDrawerLayout.isDrawerOpen(infoDrawerLayout)) {
            mDrawerLayout.closeDrawer(infoDrawerLayout);
            return;
        }

        try {
            if (getFragmentManager().findFragmentById(R.id.content_frame) instanceof StartFragment) {
                ((TextView) findViewById(R.id.description_body)).setText(getResources().getString(R.string.description_placeholder));

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
                mDrawerLayout.openDrawer(infoDrawerLayout);
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

    /** Object to listen for RoboSpice task completion. */
    private class FeedsRequestListener implements RequestListener<Category[]> {
        @Override
        public void onRequestSuccess(Category[] categories) {
            setProgressBarIndeterminateVisibility(false);
            masterFeedsList.clear();
            Collections.addAll(masterFeedsList, categories);

            updateDrawer(masterFeedsList);

            primary.notifyDataSetChanged();
            if (getFragmentManager().findFragmentById(R.id.content_frame) instanceof StartFragment && current < 0) {
                mDrawerLayout.closeDrawer(infoDrawerLayout);
                mDrawerLayout.openDrawer(navDrawerList);
            }

            if (isOnline())
                Toast.makeText(getApplicationContext(), "Feed list reloaded.", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(getApplicationContext(), "Feed list reloaded from cache. Please check internet connection.", Toast.LENGTH_LONG).show();
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
                    if (!s.equals("")) {
                        mSpiceRequest = new CategoriesRequest();
                        mSpiceRequest.setUrl(s);
                    }
                    mSpiceManager.removeAllDataFromCache();
                    listItems.clear();
                    refreshFeedsList(DurationInMillis.ALWAYS_EXPIRED);
                }
            });

            return b.create();
        }
    }

    private class DataFragment extends Fragment {
        public ArrayList<Category> saveList;
        public int current;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
        }
    }

    private void updateDrawer(ArrayList<Category> categories) {
        listItems.clear();
        for (Feed c : categories.get(0).feeds)
            listItems.add(c.title);
        primary.notifyDataSetChanged();
    }

    /**
     * Method that loads a feed into a fragment.
     * @param position Index of feed to be loaded.
     */
    private void openPreviewFragment(int position) {
        current = position;
        ImageFeedFragment iFrag = new ImageFeedFragment();

        if (getActionBar() != null) {
            Category c = masterFeedsList.get(0);
            getActionBar().setTitle(c.feeds.get(position).title);
        }

        Bundle b = new Bundle();
        b.putString("entries", masterFeedsList.get(0).feeds.get(position).entries_url);
        iFrag.setArguments(b);
        getFragmentManager().beginTransaction().replace(R.id.content_frame, iFrag, "grid").addToBackStack(null).commit();

        ((TextView) findViewById(R.id.description_body)).setText(masterFeedsList.get(0).feeds.get(current).description);
        if (masterFeedsList.get(0).feeds.get(current).more_info_url == null)
            findViewById(R.id.more_info_button).setVisibility(View.GONE);
        else
            findViewById(R.id.more_info_button).setVisibility(View.VISIBLE);
    }

    /**
     * Reloads the list of the feeds.
     * @param expiration_time Time if its been at least this long since last update, do it.
     */
    void refreshFeedsList(long expiration_time) {
        setProgressBarIndeterminateVisibility(true);
        if (!mSpiceManager.isStarted())
            mSpiceManager.start(this);
        mSpiceManager.execute(mSpiceRequest, getString(R.string.categories_cache), expiration_time, new FeedsRequestListener());
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
