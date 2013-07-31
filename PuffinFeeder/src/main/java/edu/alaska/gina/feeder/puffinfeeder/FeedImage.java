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
    private String updated_at;
    private String event_at;
    private String thumbnail;
    private String image;
    private FeedImageImages previews;

    public int getId() {
        return this.id;
    }

    public String getTitle() {
        return title;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public String getImage() {
        return image;
    }

    public FeedImageImages getPreviews() {
        return previews;
    }

    public void setEvent_at(String event_at) {
        this.event_at = event_at;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setPreviews(FeedImageImages preview) {
        this.previews = preview;
    }

    public String getEvent_at() {
        return event_at;
    }
}
