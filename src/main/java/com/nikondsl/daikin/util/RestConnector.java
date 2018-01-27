package com.nikondsl.daikin.util;

import com.nikondsl.daikin.DaikinBase;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class RestConnector {
    private static final Logger LOG = LogManager.getLogger(RestConnector.class);

    static RequestConfig createRequestConfig() {
        return RequestConfig
                .custom()
                .setConnectTimeout((int) TimeUnit.SECONDS.toMillis(5))
                .setSocketTimeout((int) TimeUnit.SECONDS.toMillis(5))
                .build();
    }

    public static List<String> submitGet(DaikinBase daikin, String path) throws IOException {
        HttpClient httpClient = HttpClientBuilder.create().setDefaultRequestConfig(createRequestConfig()).build();
        HttpGet httpGet = new HttpGet(daikin.getHost() + ":" + daikin.getPort() + path);
        HttpResponse response = httpClient.execute(httpGet);
        LOG.trace("request=" + httpGet.toString());
        return Collections.singletonList(convertStreamToString(response.getEntity().getContent()));
    }

    public static String submitPost(DaikinBase daikin, String path, String post, Map<String, String> parameters) throws IOException {
        HttpClient httpClient = HttpClientBuilder.create().setDefaultRequestConfig(createRequestConfig()).build();
        HttpPost httpPost = new HttpPost(daikin.getHost() + ":" + daikin.getPort() + path);
        List<NameValuePair> params = new ArrayList<>();
        parameters.entrySet().forEach(entry -> {
            params.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        });
        httpPost.setEntity(new UrlEncodedFormEntity(params));
        HttpResponse response = httpClient.execute(httpPost);
        LOG.trace("request=" + post);
        return EntityUtils.toString(response.getEntity());
    }
    
    static String convertStreamToString(InputStream is) {
        Scanner s = new Scanner(is,"UTF-8").useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}