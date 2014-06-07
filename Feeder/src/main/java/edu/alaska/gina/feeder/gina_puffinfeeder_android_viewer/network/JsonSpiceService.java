package edu.alaska.gina.feeder.gina_puffinfeeder_android_viewer.network;

import android.app.Application;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.octo.android.robospice.SpringAndroidSpiceService;
import com.octo.android.robospice.persistence.CacheManager;
import com.octo.android.robospice.persistence.binary.InFileInputStreamObjectPersister;
import com.octo.android.robospice.persistence.exception.CacheCreationException;
import com.octo.android.robospice.persistence.springandroid.json.jackson2.Jackson2ObjectPersisterFactory;
import com.octo.android.robospice.persistence.string.InFileStringObjectPersister;

import org.springframework.http.ContentCodingType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.util.List;

/**
 * Robospice service that handles requesting and parsing JSON objects.
 * Created by bobby on 6/14/13.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class JsonSpiceService extends SpringAndroidSpiceService {
    private static final int WEBSERVICES_TIMEOUT = 10000;

    @Override
    public int getThreadCount() {
        return 1;
    }

    @Override
    public CacheManager createCacheManager( Application application ) {
        CacheManager cacheManager = new CacheManager();

        // init
        InFileStringObjectPersister inFileStringObjectPersister;
        InFileInputStreamObjectPersister inFileInputStreamObjectPersister;
        Jackson2ObjectPersisterFactory inJSonFileObjectPersisterFactory;
        try {
            inFileStringObjectPersister = new InFileStringObjectPersister(application);
            inFileInputStreamObjectPersister = new InFileInputStreamObjectPersister(application);
            inJSonFileObjectPersisterFactory = new Jackson2ObjectPersisterFactory(application);
        } catch (CacheCreationException e) {
            e.printStackTrace();
            return null;
        }


        inFileStringObjectPersister.setAsyncSaveEnabled( true );
        inFileInputStreamObjectPersister.setAsyncSaveEnabled( true );
        inJSonFileObjectPersisterFactory.setAsyncSaveEnabled( true );

        cacheManager.addPersister( inFileStringObjectPersister );
        cacheManager.addPersister( inFileInputStreamObjectPersister );
        cacheManager.addPersister( inJSonFileObjectPersisterFactory );
        return cacheManager;
    }

    @Override
    public RestTemplate createRestTemplate() {
        RestTemplate restTemplate = new RestTemplate() {
            @Override
            protected ClientHttpRequest createRequest( URI url, HttpMethod method ) throws IOException {
                ClientHttpRequest request = super.createRequest( url, method );
                HttpHeaders headers = request.getHeaders();
                headers.setAcceptEncoding( ContentCodingType.GZIP );
                return request;
            }
        };

        // bug on http connection for Android < 2.2
        // http://android-developers.blogspot.fr/2011/09/androids-http-clients.html
        // but still a problem for upload with Spring-android on android 4.1
        System.setProperty("http.keepAlive", "false");

        // set timeout for requests
        ClientHttpRequestFactory factory = restTemplate.getRequestFactory();
        if ( factory instanceof HttpComponentsClientHttpRequestFactory) {
            HttpComponentsClientHttpRequestFactory advancedFactory = (HttpComponentsClientHttpRequestFactory) factory;
            advancedFactory.setConnectTimeout( WEBSERVICES_TIMEOUT );
            advancedFactory.setReadTimeout( WEBSERVICES_TIMEOUT );
        } else if ( factory instanceof SimpleClientHttpRequestFactory) {
            SimpleClientHttpRequestFactory advancedFactory = (SimpleClientHttpRequestFactory) factory;
            advancedFactory.setConnectTimeout( WEBSERVICES_TIMEOUT );
            advancedFactory.setReadTimeout( WEBSERVICES_TIMEOUT );
        }

        // web services support json responses
        MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
        FormHttpMessageConverter formHttpMessageConverter = new FormHttpMessageConverter();
        StringHttpMessageConverter stringHttpMessageConverter = new StringHttpMessageConverter();
        final List< HttpMessageConverter< ? >> listHttpMessageConverters = restTemplate.getMessageConverters();

        listHttpMessageConverters.add(jsonConverter);
        listHttpMessageConverters.add( formHttpMessageConverter );
        listHttpMessageConverters.add( stringHttpMessageConverter );
        restTemplate.setMessageConverters( listHttpMessageConverters );
        return restTemplate;
    }
}
