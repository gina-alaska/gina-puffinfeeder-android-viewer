package edu.alaska.gina.feeder.puffinfeeder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Object representing an image from a feed.
 * Created by bobby on 6/19/13.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FeedImage {
    private int id;
    private String title;
    private String event_at;
    private String thumbnail;
    private FeedImageImages previews;

    /**
     * Returns the ID number.
     * @return ID number.
     */
    public int getId() {
        return this.id;
    }

    /**
     * Returns the image title.
     * @return Image title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns the URL of the thumbnail.
     * @return Thumbnail's URL.
     */
    public String getThumbnail() {
        return thumbnail;
    }

    /**
     * Returns object containing the URL's of the different sizes of the image.
     * @return FeedImageImages object with all 3 image sizes.
     */
    public FeedImageImages getPreviews() {
        return previews;
    }

    /**
     * Returns the time the image was processed as a String (ISO 8601).
     * @return Time image was processed (ISO 8601).
     */
    public String getEvent_at() {
        return event_at;
    }

    /**
     * Sets the ID number.
     * @param id New ID number.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Sets the title of the feed.
     * @param title New title.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Sets the URL of the thumbnail.
     * @param thumbnail New thumbnail URL.
     */
    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    /**
     * Sets the image's FeedImageImages object that contains all the image sizes.
     * @param preview FeedImageImages object with all the different sizes of the image.
     */
    public void setPreviews(FeedImageImages preview) {
        this.previews = preview;
    }

    /**
     * Sets the time the image was processed as a String (ISO 8601).
     * @param event_at Time image was processed (ISO 8601).
     */
    public void setEvent_at(String event_at) {
        this.event_at = event_at;
    }
}
