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

        String[] params = r.getRequestURI().toString().split("\\?radius=");
        if (params.length != 2 || params[1].isEmpty()) {
            this.sendStatus(r, 400);
            return;
        }
        int rad;
        String uid;
        try{

            String [] x =  params[0].split("/");
            if(x.length != 4 || x[x.length - 1].isEmpty()){
                throw new Exception();
            }
            uid = x[x.length - 1];
            rad = Integer.parseInt(params[1]);
            if(rad <= 0){
                throw new Exception();
            }
        }
        catch(Exception e){
            this.sendStatus(r, 400);
            return;
        }

        Result result = this.dao.getUserLocationByUid(uid);
        Double user_lon;
        Double user_lat;

        if (result.hasNext()) {
            Record user = result.next();
            user_lon = user.get("n.longitude").asDouble();
            user_lat = user.get("n.latitude").asDouble();
        } else {
            this.sendStatus(r, 404);
            return;
        }

        try {
            Result drivers = this.dao.findAllDrivers();
            Value driver;
            Double driver_lon;
            Double driver_lat;
            String d_uid;
            String driver_street;

            JSONObject data = new JSONObject();
            JSONObject res = new JSONObject();
            while (drivers.hasNext()) {
                driver = drivers.next().get(0);
                JSONObject driverID = new JSONObject();
                
                d_uid = driver.get("uid").asString();
                driver_lon = driver.get("longitude").asDouble();
                driver_lat = driver.get("latitude").asDouble();
                driver_street = driver.get("street").asString();

                Result distance = this.dao.findDistance(user_lat, user_lon, driver_lat, driver_lon);
                double distanceMeters =  distance.next().get(0).asDouble()/1000;

                if(distanceMeters < rad){
                    driverID.put("longitude", driver_lon);
                    driverID.put("latitude", driver_lat);
                    driverID.put("street", driver_street);
                    data.put(d_uid, driverID);
                }

            }
            
            if (data.length() == 0){
                this.sendStatus(r, 404);
                return;
            }
            res.put("data", data);
            this.sendResponse(r, res, 200);

        } catch (Exception e) {
            e.printStackTrace();
            this.sendStatus(r, 500);
        }
    }
}
