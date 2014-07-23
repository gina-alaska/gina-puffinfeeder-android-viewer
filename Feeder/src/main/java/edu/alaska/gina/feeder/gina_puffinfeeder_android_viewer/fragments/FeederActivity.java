package edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer.fragments;

import edu.alaska.gina.feeder.android.core.data.Feed;

/**
 * Interface for fragments to use functionality within their host Activity.
 * Created by Bobby on 6/27/2014.
 */
public interface FeederActivity {
    /**
     * Method that loads a new EntriesFragment into the main content view.
     * @param newFeed New feed to be loaded.
     */
    public void openEntriesFragment(Feed newFeed);

    /**
     * Opens the navigation drawer.
     */
    public void openNavDrawer();

    /**
     * Closes the navigation drawer.
     */
    public void closeNavDrawer();
}
