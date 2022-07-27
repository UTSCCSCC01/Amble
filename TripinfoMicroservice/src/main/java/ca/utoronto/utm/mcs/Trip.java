package ca.utoronto.utm.mcs;

import com.sun.net.httpserver.HttpExchange;

import org.bson.Document;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.SQLException;

public class Trip extends Endpoint {

    /**
     * PATCH /trip/:_id
     * @param _id
     * @body distance, endTime, timeElapsed, totalCost
     * @return 200, 400, 404
     * Adds extra information to the trip with the given id when the 
     * trip is done. 
     */

    @Override
    public void handlePatch(HttpExchange r) throws IOException, JSONException {
        // check if request url isn't malformed
		String[] splitUrl = r.getRequestURI().getPath().split("/");
		if (splitUrl.length != 3) {
			this.sendStatus(r, 400);
			return;
		}

		// check if uid given is integer, return 400 if not
		String uid; 
        
		try {
			uid = splitUrl[2];
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

        String body;
		JSONObject deserialized;
        try{
            body = Utils.convert(r.getRequestBody());
		    deserialized = new JSONObject(body);
        }catch(Exception e){
            this.sendStatus(r, 400);
            return;
        }

        String[] fields = {"distance","endTime", "timeElapsed", "totalCost"};
        Class<?>[] fieldClass = {Integer.class, Integer.class, Integer.class, String.class};

        if(!this.validateFields(deserialized, fields, fieldClass)){
            this.sendStatus(r, 400);
            return;
        }

        int distance = deserialized.getInt("distance");
        int endTime = deserialized.getInt("endTime");
        int timeElapsed = deserialized.getInt("timeElapsed");
        String totalcost = deserialized.getString("totalCost");
        //Id is in the database.
        boolean flag = this.dao.updateTrip(uid, distance, endTime, timeElapsed, totalcost);
        
		if(!flag){
            this.sendStatus(r, 500);
            return;
        }
	

		this.sendStatus(r, 200);
    }
}
