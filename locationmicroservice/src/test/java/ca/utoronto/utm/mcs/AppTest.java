package ca.utoronto.utm.mcs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Please write your tests in this class. 
 */
 
public class AppTest {
    final static String API_URL = "http://localhost:8004";

    private static HttpResponse<String> sendRequest(String endpoint, String method, String reqBody) throws InterruptedException, IOException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + endpoint))
//                .version(HttpClient.Version.HTTP_1_1)
                .method(method, HttpRequest.BodyPublishers.ofString(reqBody))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public void createUser(String uid, boolean isDriver, String street) throws JSONException, IOException, InterruptedException {
        JSONObject rq = new JSONObject();
        rq.put("uid", uid);
        rq.put("is_driver", isDriver);
        sendRequest("/location/user", "PUT", rq.toString());

        JSONObject streetrq = new JSONObject();
        streetrq.put("street", street);
        streetrq.put("latitude", street);
        streetrq.put("longitude", street);
//
//        String streetRQ = String.format("{\"street\":\"%s\",\"latitude\":0.0,\"longitude\":0.0}", street);
        String street_endpoint = String.format("/location/%s", uid);

        sendRequest(street_endpoint, "PATCH", streetrq.toString());
    }

    @Test
    public void getNearbyDriver404() throws IOException, InterruptedException, JSONException {
        String uid = "90";
        int rad = 0;
        createUser(uid, true, "xyz");
        String endpoint = String.format("/location/nearbyDriver/%s?radius=%d", uid, rad);
        JSONObject rq = new JSONObject();
        HttpResponse<String> confirmRes = sendRequest(endpoint, "GET", rq.toString());
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, confirmRes.statusCode());
    }

}
