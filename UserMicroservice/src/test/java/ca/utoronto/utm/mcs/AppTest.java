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
    
    
    //@BeforeAll
    private static HttpResponse<String> sendRequest(String endpoint, String method, String reqBody) throws InterruptedException, IOException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + endpoint))
                .method(method, HttpRequest.BodyPublishers.ofString(reqBody))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }


    // Test cases
    @Test
    @Order(1)
    public void postRegister_200() throws JSONException, InterruptedException, IOException{
        JSONObject rq = new JSONObject();
            rq.put("name", "ABC");
            rq.put("email", "ABC@gmail.com");
            rq.put("password", "12345");    
        HttpResponse<String> confirmRes = sendRequest("/user/register", "POST", rq.toString());
        assertEquals(HttpURLConnection.HTTP_OK, confirmRes.statusCode());   
    }
    @Test
    @Order(2)
    public void postRegister_400() throws JSONException, InterruptedException, IOException{
        JSONObject rq = new JSONObject();
        rq.put("email", "ABC@gmail.com");
        rq.put("password", "12345");
        
        HttpResponse<String> confirmRes = sendRequest("/user/register", "POST", rq.toString());
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, confirmRes.statusCode());  
    }
    @Test
    @Order(3)
    public void postRegister_409() throws JSONException, InterruptedException, IOException{
        JSONObject rq = new JSONObject();
        rq.put("name", "ABC");
        rq.put("email", "ABC@gmail.com");
        rq.put("password", "12345");

        HttpResponse<String> confirmRes = sendRequest("/user/register", "POST", rq.toString());
        assertEquals(HttpURLConnection.HTTP_CONFLICT, confirmRes.statusCode());  
    }
    
    
    
    
    @Test
    @Order(4)
    public void postLogin_200() throws JSONException, InterruptedException, IOException{
        JSONObject rq = new JSONObject();
        rq.put("email", "ABC@gmail.com");
        rq.put("password", "12345");

        HttpResponse<String> confirmRes = sendRequest("/user/login", "POST", rq.toString());
        assertEquals(HttpURLConnection.HTTP_OK, confirmRes.statusCode());  
    }
    @Test
    @Order(5)
    public void postLogin_400() throws JSONException, InterruptedException, IOException{
        JSONObject rq = new JSONObject();
        rq.put("email", "ABC@gmail.com");
        HttpResponse<String> confirmRes = sendRequest("/user/login", "POST", rq.toString());
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, confirmRes.statusCode());  
    }
    @Test
    @Order(6)
    public void postLogin_401() throws JSONException, InterruptedException, IOException{
        JSONObject rq = new JSONObject();
        rq.put("email", "ABC@gmail.com");
        rq.put("password", "1234");

        HttpResponse<String> confirmRes = sendRequest("/user/login", "POST", rq.toString());
        assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, confirmRes.statusCode());  
    }
    @Test
    @Order(7)
    public void postLogin_404() throws JSONException, InterruptedException, IOException{
        JSONObject rq = new JSONObject();
        rq.put("email", "Xyz@gmail.com");
        rq.put("password", "1234");

        HttpResponse<String> confirmRes = sendRequest("/user/login", "POST", rq.toString());
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, confirmRes.statusCode());  
    }







}
