package edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * POJO for generic Category for Feeder Mobile API v2
 * Created by Bobby on 6/9/2014.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Category {
    public int id;
    public String name;
    public ArrayList<Feed> feeds;

    public Category() {
        //Blank Default Constructor
    }

    public Category(int id, String name, Feed[] feeds) {
        this.id = id;
        this.name = name;
        Collections.addAll(this.feeds, feeds);
    }

    @Override
    public String toString() {
        return name;
    }
}
