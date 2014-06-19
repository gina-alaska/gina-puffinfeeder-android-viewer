package edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * POJO for generic entry for Feeder Mobile API v2.
 * Created by Bobby on 6/6/2014.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Entry implements Comparable<Entry>, Comparator<Entry> {
    public int id;
    public String name;
    public String slug;
    public String url;
    public String data_url;
    public String preview_url;
    public String feed_url;
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

    public static class List {
        private static final long serialVersionUID = 6836514467436078182L;

        private ArrayList<Entry> entries;

        public ArrayList<Entry> getEntries() {
            return entries;
        }

        public void setEntries(ArrayList<Entry> users) {
            this.entries = users;
        }

        public static long getSerialversionuid() {
            return serialVersionUID;
        }
    }
}
