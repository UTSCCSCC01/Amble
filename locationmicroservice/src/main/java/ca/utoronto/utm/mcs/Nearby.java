package ca.utoronto.utm.mcs;

import java.io.IOException;
import org.json.*;
import org.neo4j.driver.*;
import com.sun.net.httpserver.HttpExchange;
import org.neo4j.driver.Record;

public class Nearby extends Endpoint {
    
    /**
     * GET /location/nearbyDriver/:uid?radius=:radius
     * @param uid, radius
     * @return 200, 400, 404, 500
     * Get drivers that are within a certain radius around a user.
        Edge Cases
            If a driver is exactly radius km away, DO NOT include their uid in the returned uids
            // ! If a driver requests a trip, include their own uid in the returned uids
     */

    @Override
    public void handleGet(HttpExchange r) throws IOException, JSONException {

        String params[] = r.getRequestURI().toString().split("/");
        if (params.length != 4 || params[3].isEmpty() ){
            this.sendStatus(r, 400);
            return;
        }
        String uidnradius[] = params[3].split("?");


        try{
            String uid = uidnradius[0];
            String rad = uidnradius[1].split("=")[1];
            Result result = this.dao.getUserLocationByUid(uid);

            if (result.hasNext()){
                JSONObject res = new JSONObject();
                
                Record user = result.next();
                Double radius = Double.parseDouble(rad);
                Result drivers = this.dao.findDriversWithinRadius(uid,radius);

                JSONObject data = new JSONObject();
                while (drivers.hasNext()){
                    Record driver = result.next();

                    Double driver_lon = driver.get("n.longitude").asDouble();
                    Double driver_lat = driver.get("n.latitude").asDouble();
                    String driver_street = driver.get("n.street").asString();

                    JSONObject driverID = new JSONObject();
                    driverID.put("longitude", driver_lon);
                    driverID.put("latitude", driver_lat);
                    driverID.put("street", driver_street);
                    data.append("driverID", driverID);
                }
                
                // Add Status
                res.put("status", "OK");
                res.put("data", data);
                
                this.sendStatus(r, 200);
            }else{
                this.sendStatus(r, 404);
            }


        }catch (Exception e){
            e.printStackTrace();
            this.sendStatus(r, 500);
        }



        
    }
}
