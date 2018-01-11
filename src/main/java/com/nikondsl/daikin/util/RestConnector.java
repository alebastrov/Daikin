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
import sun.misc.IOUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class RestConnector {

    static RequestConfig createRequestConfig() {
        return RequestConfig
                .custom()
                .setConnectTimeout((int) TimeUnit.SECONDS.toMillis(1))
                .setSocketTimeout((int) TimeUnit.SECONDS.toMillis(2))
                .build();
    }

    public static List<String> submitGet(DaikinBase daikin, String path, boolean verboseOutput) {
        try {
            HttpClient httpClient = HttpClientBuilder.create().setDefaultRequestConfig(createRequestConfig()).build();
            HttpGet httpGet = new HttpGet(daikin.getHost() + ":" + daikin.getPort() + path);
            HttpResponse response = httpClient.execute(httpGet);
            if (verboseOutput) {
                System.err.println("request=" + httpGet.toString());
                System.err.println("response=" + EntityUtils.toString(response.getEntity()));
            }
            return Collections.singletonList(new String(IOUtils.readFully(response.getEntity().getContent(), (int)response.getEntity().getContentLength(), true)));
        } catch (Exception ex) {
            return null;
        }
    }

    public static String submitPost(DaikinBase daikin, String path, String post, Map<String, String> parameters, boolean verboseOutput) {
        try {
            HttpClient httpClient = HttpClientBuilder.create().setDefaultRequestConfig(createRequestConfig()).build();
            HttpPost httpPost = new HttpPost(daikin.getHost() + ":" + daikin.getPort() + path);
            List<NameValuePair> params = new ArrayList<>();
            parameters.entrySet().forEach(entry -> {
                params.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            });
            httpPost.setEntity(new UrlEncodedFormEntity(params));
            HttpResponse response = httpClient.execute(httpPost);
            if (verboseOutput) {
                System.err.println("request=" + post);
                System.err.println("response=" + EntityUtils.toString(response.getEntity()));
            }
            return EntityUtils.toString(response.getEntity());
        } catch (Exception ex) {
            return null;
        }
    }
}