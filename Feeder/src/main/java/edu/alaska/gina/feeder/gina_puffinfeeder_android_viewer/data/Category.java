package edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;

/**
 * POJO for generic Category for Feeder Mobile API v2
 * Created by Bobby on 6/9/2014.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Category extends ArrayList<Feed> {
    public String name;

    public Category(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
