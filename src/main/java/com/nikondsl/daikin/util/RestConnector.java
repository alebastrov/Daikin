package com.nikondsl.daikin.util;

import com.nikondsl.daikin.DaikinBase;
import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.RequestLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.params.HttpParams;
import sun.misc.IOUtils;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class RestConnector {

    public static RequestConfig createRequestConfig() {
        return RequestConfig
                .custom()
                .setConnectTimeout((int) TimeUnit.SECONDS.toMillis(1))
                .setSocketTimeout((int) TimeUnit.SECONDS.toMillis(2))
                .build();
    }

    public static List<String> submitGet(DaikinBase daikin, String path, boolean verboseOutput) {
        try {
            HttpClient httpClient = HttpClientBuilder.create().setDefaultRequestConfig(createRequestConfig()).build();
            HttpResponse response = httpClient.execute(new HttpGet( daikin.getHost()+":"+daikin.getPort() + path));
            if (verboseOutput) System.err.println("response=" + response.getEntity().getContent() + "\n");
            return Collections.singletonList(new String(IOUtils.readFully(response.getEntity().getContent(), (int)response.getEntity().getContentLength(), true)));
        } catch (Exception ex) {
            return null;
        }
    }

    public static String submitPost(DaikinBase daikin, String path, String post, Map<String, String> parameters, boolean verboseOutput) {
//        try {
//            RequestSpecification spec = new RequestSpecBuilder().setBaseUri(daikin.getHost()).setPort(daikin.getPort()).build();
//            if (verboseOutput) System.err.println("request=" + post);
//            ResponseOptions response = given().spec(spec).parameters(parameters).when().post(path);
//            if (verboseOutput) System.err.println("response=" + response.body().asString() + "\n");
//            return response.body().asString();
//        } catch (Exception ex) {
//            return null;
//        }
        return null;
    }
}