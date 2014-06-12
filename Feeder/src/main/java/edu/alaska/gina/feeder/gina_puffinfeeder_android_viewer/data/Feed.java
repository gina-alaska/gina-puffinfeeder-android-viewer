package edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * POJO for generic feed for Feeder Mobile API v2.
 * Created by Bobby on 6/6/2014.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Feed {
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

    /*
    public String feed_type;
    public String data_api_url;
    public String feed_name;
    public String more_info_url;
    public String description_text;
    public String updated_at;
    public boolean online;
    public String preview_url;
    */
    @Override
    public String toString() {
        return this.title;
    }
}
