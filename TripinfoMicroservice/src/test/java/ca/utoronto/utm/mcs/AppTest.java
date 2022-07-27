package ca.utoronto.utm.mcs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.nio.charset.StandardCharsets;
/**
 * Please write your tests in this class. 
 */
 
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AppTest {
    final static String API_URL = "http://apigateway:8004";
    String t_id;

    private static HttpResponse<String> sendRequest(String endpoint, String method, String reqBody) throws InterruptedException, IOException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + endpoint))
                .method(method, HttpRequest.BodyPublishers.ofString(reqBody))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }


    // Helper Function


    // Test Cases
    @Test
    @Order(1)
    public void postReqTrip200() throws JSONException, IOException, InterruptedException {

        // Create 

        JSONObject rq = new JSONObject();
                rq.put("uid", "3");
                rq.put("radius", 10);
        HttpResponse<String> confirmRes = sendRequest("/trip/request", "POST", rq.toString());
        assertEquals(HttpURLConnection.HTTP_OK, confirmRes.statusCode());
    }
    @Test
    @Order(2)
    public void postReqTrip400() throws JSONException, IOException, InterruptedException {
        
        JSONObject rq = new JSONObject();
        rq.put("uid", "3");
        rq.put("radius", -1);
        HttpResponse<String> confirmRes = sendRequest("/trip/request", "POST", rq.toString());
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, confirmRes.statusCode());
    }
    @Test
    @Order(3)
    public void postReqTrip404() throws JSONException, IOException, InterruptedException {

        JSONObject rq = new JSONObject();
                rq.put("uid", "123");
                rq.put("radius", 10);
        HttpResponse<String> confirmRes = sendRequest("/trip/request", "POST", rq.toString());
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, confirmRes.statusCode());
    }

    @Test
    @Order(4)
    public void postConTrip200() throws JSONException, IOException, InterruptedException {

        JSONObject rq = new JSONObject();
                rq.put("driver", "4");
                rq.put("passenger", "3");
                rq.put("startTime", 222);
        HttpResponse<String> confirmRes = sendRequest("/trip/confirm", "POST", rq.toString());
        assertEquals(HttpURLConnection.HTTP_OK, confirmRes.statusCode());   
    }
    @Test
    @Order(5)
    public void postConTrip400() throws JSONException, IOException, InterruptedException {

        JSONObject rq = new JSONObject();
                rq.put("driver", "4");
                rq.put("startTime", 222);
        HttpResponse<String> confirmRes = sendRequest("/trip/confirm", "POST", rq.toString());
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, confirmRes.statusCode());   
    }
    @Test
    @Order(6)
    public void patchUpdateTrip200() throws JSONException, IOException, InterruptedException {
        JSONObject rq = new JSONObject();
                rq.put("driver", "4");
                rq.put("passenger", "3");
                rq.put("startTime", 32);
        HttpResponse<String> confirmRes = sendRequest( "/trip/confirm", "POST", rq.toString());
        t_id = new JSONObject(confirmRes.body()).getJSONObject("data").getJSONObject("_id").getString("$oid");


        String endpoint = String.format("/trip/%s", t_id);
        rq = new JSONObject();
            rq.put("distance", 2222);
            rq.put("endTime", 5422);
            rq.put("timeElapsed", 666);
            rq.put("totalCost", "130.0");

        confirmRes = sendRequest(endpoint, "PATCH", rq.toString());
        assertEquals(HttpURLConnection.HTTP_OK, confirmRes.statusCode());
    }
    @Test
    @Order(7)
    public void patchUpdateTrip400() throws JSONException, IOException, InterruptedException {
        JSONObject rq = new JSONObject();
            rq.put("distance", 2222);
            rq.put("endTime", 5422);
            rq.put("timeElapsed", 666);
            rq.put("totalCost", "130.0");

        HttpResponse<String> confirmRes = sendRequest("/trip/woho", "PATCH", rq.toString());
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, confirmRes.statusCode());
    }
    @Test
    @Order(8)
    public void getPassengerTrips200() throws JSONException, IOException, InterruptedException {
        JSONObject rq = new JSONObject();
        String endpoint = String.format("/trip/passenger/%s", 3);
        HttpResponse<String> confirmRes = sendRequest(endpoint, "GET", rq.toString());
        assertEquals(HttpURLConnection.HTTP_OK, confirmRes.statusCode());
    }
    @Test
    @Order(9)
    public void getPassengerTrips400() throws JSONException, IOException, InterruptedException {
        JSONObject rq = new JSONObject();
        String endpoint = String.format("/trip/passenger/");
        HttpResponse<String> confirmRes = sendRequest(endpoint, "GET", rq.toString());
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, confirmRes.statusCode());
    }
    @Test
    @Order(10)
    public void getPassengerTrips404() throws JSONException, IOException, InterruptedException {
        JSONObject rq = new JSONObject();
        String endpoint = String.format("/trip/passenger/56");
        HttpResponse<String> confirmRes = sendRequest(endpoint, "GET", rq.toString());
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, confirmRes.statusCode());
    }

    @Test
    @Order(11)
    public void getDriverTrips200() throws JSONException, IOException, InterruptedException {
        JSONObject rq = new JSONObject();
        String endpoint = String.format("/trip/driver/%s", 4);
        HttpResponse<String> confirmRes = sendRequest(endpoint, "GET", rq.toString());
        assertEquals(HttpURLConnection.HTTP_OK, confirmRes.statusCode());
    }
    @Test
    @Order(12)
    public void getDriverTrips400() throws JSONException, IOException, InterruptedException {
        JSONObject rq = new JSONObject();
        String endpoint = String.format("/trip/driver/");
        HttpResponse<String> confirmRes = sendRequest(endpoint, "GET", rq.toString());
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, confirmRes.statusCode());
    }
    @Test
    @Order(13)
    public void getDriverTrips404() throws JSONException, IOException, InterruptedException {
        JSONObject rq = new JSONObject();
        String endpoint = String.format("/trip/driver/56");
        HttpResponse<String> confirmRes = sendRequest(endpoint, "GET", rq.toString());
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, confirmRes.statusCode());
    }
    @Test
    @Order(14)
    public void getDriverTime200() throws JSONException, IOException, InterruptedException {
        JSONObject rq = new JSONObject();
        String endpoint = String.format("/trip/driverTime/%s", t_id);
        HttpResponse<String> confirmRes = sendRequest(endpoint, "GET", rq.toString());
        assertEquals(HttpURLConnection.HTTP_OK, confirmRes.statusCode());
    }
    @Test
    @Order(15)
    public void getDriverTime400() throws JSONException, IOException, InterruptedException {
        JSONObject rq = new JSONObject();
        String endpoint = String.format("/trip/driverTime/");
        HttpResponse<String> confirmRes = sendRequest(endpoint, "GET", rq.toString());
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, confirmRes.statusCode());
    }
    @Test
    @Order(16)
    public void getDriverTime404() throws JSONException, IOException, InterruptedException {
        JSONObject rq = new JSONObject();
        String endpoint = String.format("/trip/driverTime/900");
        HttpResponse<String> confirmRes = sendRequest(endpoint, "GET", rq.toString());
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, confirmRes.statusCode());
    }



}
