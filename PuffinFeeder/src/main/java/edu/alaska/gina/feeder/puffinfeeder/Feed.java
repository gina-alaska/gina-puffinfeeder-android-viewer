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

    /**
     * Return the feed's status as a boolean.
     * @return "true" if online, "false" otherwise.
     */
    public boolean getStatus() {
        return status;
    }

    /**
     * Returns the feed's title.
     * @return title of the feed.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns the URL of the feed's JSON object for the images.
     * @return URL of first page of feed entries.
     */
    public String getEntries() {
        return entries;
    }

    /**
     * Returns a unique identifier string ("slug") for the feed.
     * @return The feed's slug.
     */
    public String getSlug() {
        return slug;
    }

    /**
     * Returns the feed's short description.
     * @return Short description of the feed.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns location information as a string (format unknown).
     * @return Location information (format unknown).
     */
    public String getWhere() {
        return where;
    }

    /**
     * Returns the URL for the detailed description of the feed.
     * @return Detailed description URL.
     */
    public String getMoreinfo() {
        return moreinfo;
    }

    /**
     * Sets the status boolean with a String value.
     * @param status Value of status ("online" or "offline" usually).
     */
    public void setStatus(String status) {
        if (status.equals("online"))
            this.status = true;
        else
            this.status = false;
    }

    /**
     * Sets the status boolean from a boolean.
     * @param status Value to be assigned to the status.
     */
    public void setStatusBoolean(Boolean status) {
        this.status = status;
    }

    /**
     * Sets the URL where the JSON object with the image links for the feed is stored.
     * @param entries Images JSON object URL.
     */
    public void setEntries(String entries) {
        this.entries = entries;
    }

    /**
     * Sets the title of the feed.
     * @param title New feed title.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Sets the unique identifier string ("slug") of the feed.
     * @param slug new slug.
     */
    public void setSlug(String slug) {
        this.slug = slug;
    }

    /**
     * Sets the feed's description.
     * @param description New description.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Sets the location information String (format unknown).
     * @param where New location information (format unknown).
     */
    public void setWhere(String where) {
        this.where = where;
    }

    /**
     * Sets the URL where the detailed feed description is stored.
     * @param moreinfo URL where detailed description is stored.
     */
    public void setMoreinfo(String moreinfo) {
        this.moreinfo = moreinfo;
    }
}
