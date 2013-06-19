package edu.alaska.gina.feeder.puffinfeeder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FeedsJsonRequest extends SpringAndroidSpiceRequest {

    public FeedsJsonRequest() {
        super(Feed[].class);
    }

    @Override
    public Feed[] loadDataFromNetwork() throws Exception {
        return getRestTemplate().getForObject("http://feeder.gina.alaska.edu/feeds.json", Feed[].class);
    }
}
