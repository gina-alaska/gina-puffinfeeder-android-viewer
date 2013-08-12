package edu.alaska.gina.feeder.puffinfeeder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Object representing all links to all sizes of one image.
 * Created by bobby on 7/26/13.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FeedImageImages {
    private String small;
    private String medium;
    private String large;

    /**
     * Returns the URL of the small (500x500px) image.
     * @return URL of small image.
     */
    public String getSmall() {
        return small;
    }

    /**
     * Returns the URL of the medium (1000x1000px) image.
     * @return URL of medium image.
     */
    public String getMedium() {
        return medium;
    }

    /**
     * Returns URL of large (2000x2000px) image.
     * @return URL of large image.
     */
    public String getLarge() {
        return large;
    }

    /**
     * Returns String Array with URL's of all image sizes.
     * {small, medium, large}.
     * @return URL's of all image sizes.
     */
    public String[] getAll() {
        return new String[]{small, medium, large};
    }

    /**
     * Sets the small image URL.
     * @param small New small image URL.
     */
    public void setSmall(String small) {
        this.small = small;
    }

    /**
     * Sets the medium image URL.
     * @param medium New medium image URL.
     */
    public void setMedium(String medium) {
        this.medium = medium;
    }

    /**
     * Sets large image URL.
     * @param large New large image URL.
     */
    public void setLarge(String large) {
        this.large = large;
    }
}
