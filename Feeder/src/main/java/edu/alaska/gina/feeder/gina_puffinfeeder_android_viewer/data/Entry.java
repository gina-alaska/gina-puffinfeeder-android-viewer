package edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * POJO for generic entry for Feeder Mobile API v2.
 * Created by Bobby on 6/6/2014.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Entry implements Comparable<Entry>, Comparator<Entry> {
    public int id;
    public String slug;
    public DateTime event_at;
    public String url;
    public String source_url;
    public String data_url;
    public String preview_url;
    public boolean starred;

    @Override
    public int compareTo(Entry another) {
        if (this.id < another.id)
            return -1;
        if (this.id > another.id)
            return 1;
        return 0;
    }

    @Override
    public int compare(Entry lhs, Entry rhs) {
        if (lhs.id < rhs.id)
            return -1;
        if (lhs.id > rhs.id)
            return 1;
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Entry)
            return this.id == ((Entry) o).id;
        return false;
    }
}
