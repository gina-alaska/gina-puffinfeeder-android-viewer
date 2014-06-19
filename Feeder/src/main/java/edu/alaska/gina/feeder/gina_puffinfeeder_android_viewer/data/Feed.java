package edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Comparator;

/**
 * POJO for generic feed for Feeder Mobile API v2.
 * Created by Bobby on 6/6/2014.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Feed implements Comparator<Feed>, Comparable<Feed> {
    public int id;
    public String title;
    public String slug;
    public String description;
    public String type;
    public String updated_at;
    public boolean online;
    public String preview_url;
    public String entries_url;
    public String more_info_url;
    public boolean mobile_compatible;
    public boolean browser_compatible;

    @Override
    public String toString() {
        return this.title;
    }

    @Override
    public int compareTo(Feed another) {
        return this.title.compareTo(another.title);
    }

    @Override
    public int compare(Feed lhs, Feed rhs) {
        return lhs.title.compareTo(rhs.title);
    }
}
