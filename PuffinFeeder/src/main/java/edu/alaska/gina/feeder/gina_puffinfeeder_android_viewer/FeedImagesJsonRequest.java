package edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;

/**
 * Spring/Robospice request to grab image feed JSON array from feeder.
 * Created by bobby on 6/19/13.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FeedImagesJsonRequest extends SpringAndroidSpiceRequest<FeedImage[]> {
    Feed feed;
    int page;

    public FeedImagesJsonRequest(Feed feed, int page) {
        super(FeedImage[].class);
        this.feed = feed;
        this.page = page;
    }

    @Override
    public FeedImage[] loadDataFromNetwork() throws Exception {
        return getRestTemplate().getForObject(feed.getEntries() + "?page=" + page, FeedImage[].class);
    }
}
