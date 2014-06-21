package edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer.network;

import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;
import edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer.data.Category;

/**
 * Created by Bobby on 6/6/2014.
 */
public class CategoriesRequest extends SpringAndroidSpiceRequest<Category[]> {
    private String url;

    public CategoriesRequest() {
        super(Category[].class);
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public Category[] loadDataFromNetwork() throws Exception {
        return getRestTemplate().getForObject(url, Category[].class);
    }
}
