package ca.utoronto.utm.mcs;

/** 
 * Everything you need in order to send and recieve httprequests to 
 * other microservices is given here. Do not use anything else to send 
 * and/or recieve http requests from other microservices. Any other 
 * imports are fine.
 */
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;

import com.sun.net.httpserver.HttpExchange;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Iterator;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Request extends Endpoint {

    /**
     * POST /trip/request
     * @body uid, radius
     * @return 200, 400, 404, 500
     * Returns a list of drivers within the specified radius 
     * using location microservice. List should be obtained
     * from navigation endpoint in location microservice
     */

    @Override
    public void handlePost(HttpExchange r) throws IOException,JSONException{

		String[] splitUrl = r.getRequestURI().getPath().split("/");
		if (splitUrl.length != 3) {
			this.sendStatus(r, 400);
			return;
		}

		// check if email, password given and of string, return 400 if not
        String body;
		JSONObject deserialized;
        try{
            body = Utils.convert(r.getRequestBody());
		    deserialized = new JSONObject(body);
        }catch(Exception e){
            this.sendStatus(r, 400);
            return;
        }

        String[] fields = {"uid","radius"};
        Class<?>[] fieldClass = {String.class, Integer.class};

        if(!this.validateFields(deserialized, fields, fieldClass)){
            this.sendStatus(r, 400);
            return;
        }

        String uid = deserialized.getString("uid");
        int radius = deserialized.getInt("radius");

        if(radius < 0){
            this.sendStatus(r, 400);
            return;
        }


        HttpResponse<InputStream> response = null;

        //Sends a http request to the location microservice /nearbydriver to get a json payload.
        try{
            // System.out.println("--------Starting to send request--------------");

            //Verified
            String url = "http://locationmicroservice:8000/location/nearbyDriver/%s?radius=%d";
            url = String.format(url, uid, radius);
            HttpClient http_client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();
            
            //Send Response
            // System.out.println("Gonna start running the send req");
            response = http_client.send(request, BodyHandlers.ofInputStream());
            // System.out.println("--------Finished to send request--------------");
        }catch(Exception e){
            this.sendStatus(r, 500);
            return;
        }

        if(response == null){
            this.sendStatus(r, 500);
            return;
        }

        //Precondition:
        // response is not null and contains data from the server. 
        String body_server;
		JSONObject deserialized_body_server;
        try{
            body_server = Utils.convert(response.body());
		    deserialized_body_server = new JSONObject(body_server);
        }catch(Exception e){

            this.sendStatus(r, 500);
            return;
        }

        if(response.statusCode() != 200){
            this.sendResponse(r, deserialized_body_server, response.statusCode());
            return;
        }

        JSONObject data = deserialized_body_server.getJSONObject("data");
        //Precondition:
        // server responded with good data and is in the desearlized_body_server
        
        JSONArray data_value = new JSONArray();

        Iterator<String> keys = data.keys();
        while(keys.hasNext()){
            String key = keys.next();
            data_value.put(key);
        }

        JSONObject final_response = new JSONObject();
        
        final_response.put("data", data_value);
        
        this.sendResponse(r, final_response, 200);
    }
}
