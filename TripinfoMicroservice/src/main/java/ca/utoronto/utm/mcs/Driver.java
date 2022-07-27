package ca.utoronto.utm.mcs;

import com.mongodb.client.FindIterable;
import com.sun.net.httpserver.HttpExchange;
import org.json.JSONException;
import java.io.IOException;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class Driver extends Endpoint {

    /**
     * GET /trip/driver/:uid
     * @param uid
     * @return 200, 400, 404
     * Get all trips driver with the given uid has.
     */

    @Override
    public void handleGet(HttpExchange r) throws IOException, JSONException {
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
		FindIterable<Document> doc;
		try {
			doc = this.dao.getDriverTrip(uid);
            if(!doc.iterator().hasNext()){
                //user has never been a driver.
                this.sendStatus(r, 404);
                return;
            }
		} 
		catch (Exception e) {
            e.printStackTrace();
			this.sendStatus(r, 500);
			return;
		}

        JSONArray trip_payload = new JSONArray();
        try{
            for(Document items: doc){
                String rep = items.toJson().toString();
		        JSONObject result = new JSONObject(rep);
                result.put("_id", result.getJSONObject("_id").get("$oid"));
                result.remove("driver");
                trip_payload.put(result);
            }
        }catch(Exception e){
            e.printStackTrace();
            this.sendStatus(r, 500);
            return;
        }
        
        JSONObject response = new JSONObject();
        response.put("data", trip_payload);
        

		this.sendResponse(r, response, 200);
    
    }
}
