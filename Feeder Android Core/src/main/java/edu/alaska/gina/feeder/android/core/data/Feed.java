package edu.alaska.gina.feeder.android.core.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.Comparator;

/**
 * POJO for generic feed for Feeder Mobile API v2.
 * Created by Bobby on 6/6/2014.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Feed implements Comparator<Feed>, Comparable<Feed>, Serializable {
    public int id;
    public String title;
    public String slug;
    public String description;
    public DateTime updated_at;
    public String mobile_compatible;
    public String url;
    public boolean online;
    public String category;
    public String preview_url;
    public String entries_url;
    public String more_info_url;

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof Feed && ((Feed) obj).id == this.id);
    }

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
