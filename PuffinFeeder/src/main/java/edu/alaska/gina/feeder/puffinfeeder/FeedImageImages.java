package edu.alaska.gina.feeder.puffinfeeder;

/**
 * Object representing all links to all sizes of one image.
 * Created by bobby on 7/26/13.
 */
public class FeedImageImages {
    private String small;
    private String medium;
    private String large;

    public String getSmall() {
        return small;
    }

    public String getMedium() {
        return medium;
    }

    public String getLarge() {
        return large;
    }

    public String[] getAll() {
        return new String[]{small, medium, large};
    }

    public void setSmall(String small) {
        this.small = small;
    }

    public void setMedium(String medium) {
        this.medium = medium;
    }

    public void setLarge(String large) {
        this.large = large;
    }
}
