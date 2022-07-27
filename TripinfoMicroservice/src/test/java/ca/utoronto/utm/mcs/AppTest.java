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
    final static String API_URL = "http://tripinfomicroservice:8000";
    public String t_id;

    private static HttpResponse<String> sendRequest(String endpoint, String method, String reqBody) throws InterruptedException, IOException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + endpoint))
                .method(method, HttpRequest.BodyPublishers.ofString(reqBody))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
    private static HttpResponse<String> sendRequest(String URL, String endpoint, String method, String reqBody) throws InterruptedException, IOException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL + endpoint))
                .method(method, HttpRequest.BodyPublishers.ofString(reqBody))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }


    // Helper Function
        // Create User
    public void createUser(String uid, boolean isDriver, String street) throws JSONException, IOException, InterruptedException {
        //WORKS
        JSONObject rq = new JSONObject();
        rq.put("uid", uid);
        rq.put("is_driver", isDriver);
        HttpResponse<String> test1 = sendRequest("http://locationmicroservice:8000","/location/user", "PUT", rq.toString());

        //PRECONDITION: User has been added to the database already. 
        JSONObject streetrq = new JSONObject();
        double x = 0.0;
        streetrq.put("latitude", x);
        streetrq.put("longitude", x);
        streetrq.put("street", street);
        //verify payload
        String street_endpoint = String.format("/location/%s", uid);
        String streetRQ = String.format("{\"street\":\"%s\",\"latitude\":0.0,\"longitude\":0.0}", street);
        
        HttpResponse<String> test = sendRequest("http://locationmicroservice:8000",street_endpoint, "PATCH", streetRQ);
        // System.out.println(String.valueOf(test1.statusCode()) + test1.body().toString());
        // System.out.println(String.valueOf(test.statusCode()) + test.body().toString());
    }
        // Join Roads    
    public void join_roads(String road_1, String road_2) throws JSONException, InterruptedException, IOException{
        // Adding Roads
        JSONObject rq = new JSONObject();
                rq.put("roadName", road_1);
                rq.put("hasTraffic", false);
        sendRequest("http://locationmicroservice:8000","/location/road", "PUT", rq.toString());
        rq = new JSONObject();
                rq.put("roadName", road_2);
                rq.put("hasTraffic", true);
        sendRequest("http://locationmicroservice:8000","/location/road", "PUT", rq.toString());
        // Join the road
        rq = new JSONObject();
                rq.put("roadName1", road_1);
                rq.put("roadName2", road_2);
                rq.put("hasTraffic", true);
                rq.put("time", 60);
        sendRequest("http://locationmicroservice:8000","/location/hasRoute", "POST", rq.toString());
    }
        // 


        
    //


    // Test Cases
    @Test
    @Order(1)
    public void postReqTrip200() throws JSONException, IOException, InterruptedException {
        // Create passenger (3)
        createUser("3", false, "road_2");

        // Create driver (4)
        createUser("4", true, "road_1");

        // Join roads
        join_roads("road_1", "road_2");

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

        HttpResponse<String> confirmRes = sendRequest("/trip/", "PATCH", rq.toString());
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, confirmRes.statusCode());
    }

    @Test
    @Order(8)
    public void patchUpdateTrip404() throws JSONException, IOException, InterruptedException {
        JSONObject rq = new JSONObject();
            rq.put("distance", 2222);
            rq.put("endTime", 5422);
            rq.put("timeElapsed", 666);
            rq.put("totalCost", "130.0");

        HttpResponse<String> confirmRes = sendRequest("/trip/wooohi", "PATCH", rq.toString());
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, confirmRes.statusCode());
    }
    
    

    @Test
    @Order(9)
    public void getPassengerTrips200() throws JSONException, IOException, InterruptedException {
        
        
        JSONObject rq = new JSONObject();
        String endpoint = String.format("/trip/passenger/%s", "3");
        HttpResponse<String> confirmRes = sendRequest(endpoint, "GET", rq.toString());
        assertEquals(HttpURLConnection.HTTP_OK, confirmRes.statusCode());
    }

    @Test
    @Order(10)
    public void getPassengerTrips400() throws JSONException, IOException, InterruptedException {
        JSONObject rq = new JSONObject();
        String endpoint = String.format("/trip/passenger/");
        HttpResponse<String> confirmRes = sendRequest(endpoint, "GET", rq.toString());
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, confirmRes.statusCode());
    }


    @Test
    @Order(11)
    public void getPassengerTrips404() throws JSONException, IOException, InterruptedException {
        JSONObject rq = new JSONObject();
        String endpoint = String.format("/trip/passenger/56");
        HttpResponse<String> confirmRes = sendRequest(endpoint, "GET", rq.toString());
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, confirmRes.statusCode());
    }



    
    @Test
    @Order(12)
    public void getDriverTrips200() throws JSONException, IOException, InterruptedException {
        JSONObject rq = new JSONObject();
        String endpoint = String.format("/trip/driver/%s", "4");
        HttpResponse<String> confirmRes = sendRequest(endpoint, "GET", rq.toString());
        assertEquals(HttpURLConnection.HTTP_OK, confirmRes.statusCode());
    }



    @Test
    @Order(13)
    public void getDriverTrips400() throws JSONException, IOException, InterruptedException {
        JSONObject rq = new JSONObject();
        String endpoint = String.format("/trip/driver/");
        HttpResponse<String> confirmRes = sendRequest(endpoint, "GET", rq.toString());
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, confirmRes.statusCode());
    }
    

    @Test
    @Order(14)
    public void getDriverTrips404() throws JSONException, IOException, InterruptedException {
        JSONObject rq = new JSONObject();
        String endpoint = String.format("/trip/driver/56");
        HttpResponse<String> confirmRes = sendRequest(endpoint, "GET", rq.toString());
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, confirmRes.statusCode());
    }
    


    
    @Test
    @Order(15)
    public void getDriverTime200() throws JSONException, IOException, InterruptedException {
        // Create a trip
        JSONObject rq = new JSONObject();
                rq.put("driver", "4");
                rq.put("passenger", "3");
                rq.put("startTime", 32);
        HttpResponse<String> confirmRes = sendRequest( "/trip/confirm", "POST", rq.toString());
        String t_id1 = new JSONObject(confirmRes.body()).getJSONObject("data").getJSONObject("_id").getString("$oid");
        // System.out.printf("____%s____\n\n", confirmRes.body().toString());

        
        rq = new JSONObject();
        // System.out.printf("____%s____", t_id1);
        String endpoint = String.format("/trip/driverTime/%s", t_id1);
        confirmRes = sendRequest(endpoint, "GET", rq.toString());
        // System.out.printf("____%s____\n", confirmRes.body().toString());
        assertEquals(HttpURLConnection.HTTP_OK, confirmRes.statusCode());
    }


    @Test
    @Order(16)
    public void getDriverTime400() throws JSONException, IOException, InterruptedException {
        JSONObject rq = new JSONObject();
        String endpoint = String.format("/trip/driverTime/");
        HttpResponse<String> confirmRes = sendRequest(endpoint, "GET", rq.toString());
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, confirmRes.statusCode());
    }
    @Test
    @Order(17)
    public void getDriverTime404() throws JSONException, IOException, InterruptedException {
        JSONObject rq = new JSONObject();
        String endpoint = String.format("/trip/driverTime/900");
        HttpResponse<String> confirmRes = sendRequest(endpoint, "GET", rq.toString());
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, confirmRes.statusCode());
    }



}
