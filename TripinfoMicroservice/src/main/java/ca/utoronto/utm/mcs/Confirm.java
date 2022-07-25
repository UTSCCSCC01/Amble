package ca.utoronto.utm.mcs;

import com.sun.net.httpserver.HttpExchange;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class Confirm extends Endpoint {

    /**
     * POST /trip/confirm
     * @body driver, passenger, startTime
     * @return 200, 400
     * Adds trip info into the database after trip has been requested.
     */

    @Override
    public void handlePost(HttpExchange r) throws IOException, JSONException {
        // check if request url isn't malformed
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

        String[] fields = {"driver","passenger", "startTime"};
        Class<?>[] fieldClass = {String.class, String.class, Integer.class};

        if(!this.validateFields(deserialized, fields, fieldClass)){
            this.sendStatus(r, 400);
            return;
        }

        String driver = deserialized.getString("driver");
        String passenger = deserialized.getString("passenger");
        int startTime = deserialized.getInt("startTime");

        JSONObject response;

        try{
            response = this.dao.addTrip(driver, passenger, startTime);
        }catch(Exception e){
            e.printStackTrace();
			this.sendStatus(r, 500);
			return;
        }

        this.sendResponse(r, response, 200);
    }
}
