package edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * POJO for generic entry for Feeder Mobile API v2.
 * Created by Bobby on 6/6/2014.
 */
//TODO Update this class to actual JSON field names
@JsonIgnoreProperties(ignoreUnknown = true)
public class Entry {
    public String new_date;
    public String source_url;
    public String data_url;
    public String title_new;
    public String preview;
    public boolean starred;
    public String id_new;
}
