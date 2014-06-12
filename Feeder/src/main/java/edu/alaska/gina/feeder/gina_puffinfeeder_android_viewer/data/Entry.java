package edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * POJO for generic entry for Feeder Mobile API v2.
 * Created by Bobby on 6/6/2014.
 */
//TODO Update this class to actual JSON field names
@JsonIgnoreProperties(ignoreUnknown = true)
public class Entry {
    public int id;
    public String name;
    public String slug;
    public String url;
    public String data_url;
    public String preview_url;
    public String feed_url;
    public boolean starred;
}
