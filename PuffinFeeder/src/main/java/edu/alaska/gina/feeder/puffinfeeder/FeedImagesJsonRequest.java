package edu.alaska.gina.feeder.puffinfeeder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;

/**
 * Kick-starts feed image metadata grabbing process.
 * Created by bobby on 6/19/13.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FeedImagesJsonRequest extends SpringAndroidSpiceRequest<FeedImage[]> {
    Feed feed;

    public FeedImagesJsonRequest(Feed feed) {
        super(FeedImage[].class);
        this.feed = feed;
    }

    @Override
    public FeedImage[] loadDataFromNetwork() throws Exception {
        return getRestTemplate().getForObject(feed.getEntries(), FeedImage[].class);
    }
}
