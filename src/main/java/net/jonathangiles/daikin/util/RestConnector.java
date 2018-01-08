package net.jonathangiles.daikin.util;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.response.ResponseOptions;
import io.restassured.specification.RequestSpecification;
import net.jonathangiles.daikin.IDaikin;


import java.util.Collections;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class RestConnector {

    public static List<String> submitGet(IDaikin daikin, String path, boolean verboseOutput) {
        try {
            RequestSpecification spec = new RequestSpecBuilder().setBaseUri(daikin.getHost()).setPort(80).build();
            if (verboseOutput) System.err.println("request=" + daikin.getHost() + path);
            ResponseOptions response = given().spec(spec).get(path);
            if (verboseOutput) System.err.println("response=" + response.body().asString() + "\n");
            return Collections.singletonList(response.getBody().asString());
        } catch (Exception ex) {
            return null;
        }
    }

    public static String submitPost(String hostName, String path, String post, Map<String, String> parameters, boolean isVerboseOutput) {
        try {
            RequestSpecification spec = new RequestSpecBuilder().setBaseUri(hostName).setPort(80).build();


            if (isVerboseOutput) System.err.println("request=" + post);
            ResponseOptions response = given().spec(spec).parameters(parameters).when().post(path);
            if (isVerboseOutput) System.err.println("response=" + response.body().asString() + "\n");
            return response.body().asString();
        } catch (Exception ex) {
            return null;
        }
    }
}