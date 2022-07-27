package ca.utoronto.utm.mcs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Please write your tests in this class.
 */

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AppTest {
    final static String API_URL = "http://apigateway:8004";

    private static HttpResponse<String> sendRequest(String endpoint, String method, String reqBody) throws InterruptedException, IOException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + endpoint))
//                .version(HttpClient.Version.HTTP_1_1)
                .method(method, HttpRequest.BodyPublishers.ofString(reqBody))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    // @BeforeAll
    public void createUser(String uid, boolean isDriver, String street) throws JSONException, IOException, InterruptedException {
        JSONObject rq = new JSONObject();
        rq.put("uid", uid);
        rq.put("is_driver", isDriver);
        sendRequest("/location/user", "PUT", rq.toString());

        JSONObject streetrq = new JSONObject();
        streetrq.put("street", street);
        streetrq.put("latitude", street);
        streetrq.put("longitude", street);
        // String streetRQ = String.format("{\"street\":\"%s\",\"latitude\":0.0,\"longitude\":0.0}", street);
        String street_endpoint = String.format("/location/%s", uid);
        sendRequest(street_endpoint, "PATCH", streetrq.toString());
    }
    
    public void join_roads(String road_1, String road_2) throws JSONException, InterruptedException, IOException{
        // Adding Roads
        JSONObject rq = new JSONObject();
                rq.put("roadName", road_1);
                rq.put("hasTraffic", false);
        sendRequest("/location/road", "PUT", rq.toString());
        rq = new JSONObject();
                rq.put("roadName", road_2);
                rq.put("hasTraffic", true);
        sendRequest("/location/road", "PUT", rq.toString());
        // Join the road
        rq = new JSONObject();
                rq.put("roadName1", road_1);
                rq.put("roadName2", road_2);
                rq.put("hasTraffic", true);
                rq.put("time", 60);
        sendRequest("/location/hasRoute", "POST", rq.toString());
    }


    @Test
    @Order(1)
    public void getNearbyDriver200() throws IOException, InterruptedException, JSONException {
        createUser("90", true, "xyz");
        
        int rad = 10;
        String endpoint = String.format("/location/nearbyDriver/%s?radius=%d", uid, rad);
        JSONObject rq = new JSONObject();
        HttpResponse<String> confirmRes = sendRequest(endpoint, "GET", rq.toString());
        assertEquals(HttpURLConnection.HTTP_OK, confirmRes.statusCode());
    }
    @Test
    @Order(2)
    public void getNearbyDriver404() throws IOException, InterruptedException, JSONException {
        String endpoint = String.format("/location/nearbyDriver/90?radius=0");
        JSONObject rq = new JSONObject();
        HttpResponse<String> confirmRes = sendRequest(endpoint, "GET", rq.toString());
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, confirmRes.statusCode());
    }
    @Test
    @Order(3)
    public void getNearbyDriver400() throws IOException, InterruptedException, JSONException {
        String endpoint = String.format("/location/nearbyDriver/?radius=0");
        JSONObject rq = new JSONObject();
        HttpResponse<String> confirmRes = sendRequest(endpoint, "GET", rq.toString());
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, confirmRes.statusCode());
    }






    @Test
    @Order(4)
    public void getNavRoute200() throws IOException, InterruptedException, JSONException {
        createUser("1", true, "road_1");
        createUser("2", false, "road_2");
        join_roads("road_1", "road_1");

        String endpoint = String.format("/location/navigation/1?passengerUid=2");
        JSONObject rq = new JSONObject();
        HttpResponse<String> confirmRes = sendRequest(endpoint, "GET", rq.toString());
        assertEquals(HttpURLConnection.HTTP_OK, confirmRes.statusCode());
    }
    @Test
    @Order(5)
    public void getNavRouter404() throws IOException, InterruptedException, JSONException {
        String endpoint = String.format("/location/navigation/3?passengerUid=1");
        JSONObject rq = new JSONObject();
        HttpResponse<String> confirmRes = sendRequest(endpoint, "GET", rq.toString());
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, confirmRes.statusCode());
    }
    @Test
    @Order(6)
    public void getNavRoute400() throws IOException, InterruptedException, JSONException {
        String endpoint = String.format("/location/navigation/?passengerUid=2");
        JSONObject rq = new JSONObject();
        HttpResponse<String> confirmRes = sendRequest(endpoint, "GET", rq.toString());
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, confirmRes.statusCode());
    }







}