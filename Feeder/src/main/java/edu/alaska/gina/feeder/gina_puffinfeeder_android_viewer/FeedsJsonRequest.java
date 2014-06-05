package edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;

/**
 * Spring/Robospice request to grab the feeds JSON array from feeder.
 * Created by bobby on 6/14/13.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FeedsJsonRequest extends SpringAndroidSpiceRequest {
    public String baseURL;

    public FeedsJsonRequest() {
        super(Feed[].class);
        baseURL = "http://feeder.gina.alaska.edu/feeds.json";
    }

    @Override
    public Feed[] loadDataFromNetwork() throws Exception {
        return getRestTemplate().getForObject(baseURL, Feed[].class);
    }
}
