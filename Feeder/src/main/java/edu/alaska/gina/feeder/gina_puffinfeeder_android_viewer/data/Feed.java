package edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * POJO for generic feed for Feeder Mobile API v2.
 * Created by Bobby on 6/6/2014.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Feed {
    public String feed_type;
    public String data_api_url;
    public String feed_name;
    public String more_info_url;
    public String description_text;
    public String updated_at;
    public boolean online;
    public String preview_url;
}
