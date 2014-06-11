package edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer.data;

import java.util.ArrayList;

/**
 * Object that holds a list of Feeds
 * Created by Bobby on 6/9/2014.
 */
public class FeedsList extends ArrayList<Feed> {
    public String name;

    public FeedsList(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
