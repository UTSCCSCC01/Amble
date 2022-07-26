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

import com.mongodb.client.FindIterable;
import com.sun.net.httpserver.HttpExchange;

import org.bson.Document;
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


public class Drivetime extends Endpoint {

    /**
     * GET /trip/driverTime/:_id
     * @param _id
     * @return 200, 400, 404, 500
     * Get time taken to get from driver to passenger on the trip with
     * the given _id. Time should be obtained from navigation endpoint
     * in location microservice.
     */

    @Override
    public void handleGet(HttpExchange r) throws IOException, JSONException {


        // check if request url isn't malformed
	    // check if request url isn't malformed
        String[] splitUrl = r.getRequestURI().getPath().split("/");
        if (splitUrl.length != 4) {
            this.sendStatus(r, 400);
            return;
        }
    
        // check if uid given is integer, return 400 if not
        String uid; 
            
        try {
            uid = splitUrl[3];
        } catch (Exception e) {
            e.printStackTrace();
            this.sendStatus(r, 400);
            return;
        }
    
        // make a query to see if a tripid exists. 
        Document doc;
        try {
            doc = this.dao.getTrip(uid);
            if(doc == null){
                this.sendStatus(r, 404);
                return;
            }
        } 
        catch (Exception e) {
            e.printStackTrace();
            this.sendStatus(r, 500);
            return;
        }

        String rep = doc.toJson().toString();
		JSONObject result = new JSONObject(rep);

        //get uid, driver id
        String passengerid;
        String driverid;
        passengerid = result.getString("passenger");
        driverid = result.getString("driver");

        if(passengerid == null || driverid == null){
            System.out.println("ERROR FOR FETCHING PASS/DRIVER ID");
        }
        
        //Precondition: passengerId,driverId not null. 

        //Send an http post request to the microservice and get results back.
        HttpResponse<InputStream> response = null;

        try{
            String url = "http://locationmicroservice:8000/location/navigation/%s?passengerUid=%s";
            url = String.format(url, driverid, passengerid);
            HttpClient http_client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();

            response = http_client.send(request, BodyHandlers.ofInputStream());

        }catch(Exception e){
            this.sendStatus(r, 500);
            return;
        }

        if(response == null){
            this.sendStatus(r, 500);
            return;
        }

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
        int total_time = data.getInt("total_time");

        //Prepare final payload

        JSONObject release_payload = new JSONObject();

        JSONObject data_release_payload = new JSONObject();
        data_release_payload.put("arrival_time", total_time);

        release_payload.put("data", data_release_payload);

        this.sendResponse(r, release_payload, 200);
    }
}
