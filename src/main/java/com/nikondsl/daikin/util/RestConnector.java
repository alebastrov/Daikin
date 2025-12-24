package com.nikondsl.daikin.util;

import com.nikondsl.daikin.CommandMode;
import com.nikondsl.daikin.DaikinBase;
import lombok.extern.log4j.Log4j2;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.util.Timeout;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

@Log4j2
public class RestConnector {
    private RestConnector() {
    }

    static RequestConfig createRequestConfig(CommandMode commandMode) {
        int timeoutInSeconds = commandMode == CommandMode.SCANNING ? 1 : 5;
        return RequestConfig
                .custom()
                .setConnectionRequestTimeout(Timeout.of(Duration.of(timeoutInSeconds, ChronoUnit.SECONDS)))
                .build();
    }

    public static List<String> submitGet(DaikinBase daikin, String path, CommandMode commandMode) throws IOException {
        CloseableHttpClient httpClient = HttpClientBuilder
                .create()
                .setDefaultRequestConfig(createRequestConfig(commandMode))
                .build();
        HttpGet httpGet = new HttpGet(daikin.getHost() + ":" + daikin.getPort() + path);
        return Collections.singletonList(httpClient.execute(httpGet, response -> {
            log.trace("request=" + httpGet);
            return convertStreamToString(response.getEntity().getContent());
        }));

    }

    public static String submitPost(DaikinBase daikin, String path, String post, Map<String, String> parameters) throws IOException {
        CloseableHttpClient httpClient = HttpClientBuilder.create().setDefaultRequestConfig(createRequestConfig(CommandMode.COMMAND)).build();
        HttpPost httpPost = new HttpPost(daikin.getHost() + ":" + daikin.getPort() + path);
        List<NameValuePair> params = new ArrayList<>();
        parameters.entrySet().forEach(entry ->
            params.add(new BasicNameValuePair(entry.getKey(), entry.getValue()))
        );
        httpPost.setEntity(new UrlEncodedFormEntity(params));
        return httpClient.execute(httpPost, response -> {
            log.trace("request=" + httpPost);
            log.trace("response=" + response);
            return EntityUtils.toString(response.getEntity());
        });
    }
    
    static String convertStreamToString(InputStream is) {
        Scanner s = new Scanner(is,"UTF-8").useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}