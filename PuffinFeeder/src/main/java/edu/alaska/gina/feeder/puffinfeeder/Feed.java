package edu.alaska.gina.feeder.puffinfeeder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Object used to represent a single feed.
 * Created by bobby on 6/14/13.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Feed {
    private boolean status;
    private String title;
    private String entries;
    private String slug;
    private String description;
    private String where;
    private String moreinfo;

    public boolean getStatus() {
        return status;
    }

    public String getTitle() {
        return title;
    }

    public String getEntries() {
        return entries;
    }

    public String getSlug() {
        return slug;
    }

    public String getDescription() {
        return description;
    }

    public String getWhere() {
        return where;
    }

    public String getMoreinfo() {
        return moreinfo;
    }

    public void setStatus(String status) {
        if (status.equals("online"))
            this.status = true;
        else
            this.status = false;
    }

    public void setStatusBoolean(Boolean status) {
        this.status = status;
    }

    public void setEntries(String entries) {
        this.entries = entries;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setWhere(String where) {
        this.where = where;
    }

    public void setMoreinfo(String moreinfo) {
        this.moreinfo = moreinfo;
    }
}
