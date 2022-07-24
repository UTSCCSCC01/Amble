package ca.utoronto.utm.mcs;
import java.io.IOException;
import org.json.*;
import org.neo4j.driver.*;
import com.sun.net.httpserver.HttpExchange;
import org.neo4j.driver.Record;

public class Navigation extends Endpoint {
    
    /**
     * GET /location/navigation/:driverUid?passengerUid=:passengerUid
     * @param driverUid, passengerUid
     * @return 200, 400, 404, 500
     * Get the shortest path from a driver to passenger weighted by the
     * travel_time attribute on the ROUTE_TO relationship.
     */

    @Override
    public void handleGet(HttpExchange r) throws IOException, JSONException {
        
        String params[] = r.getRequestURI().toString().split("/");
        if (params.length != 4 || params[3].isEmpty() ){
            this.sendStatus(r, 400);
            return;
        }
        String dUIDnUID[] = params[3].split("?");


        try{
            String duid = dUIDnUID[0];
            String uid = dUIDnUID[1].split("=")[1];
            Result result = this.dao.getUserLocationByUid(uid);
            Result result1 = this.dao.getUserLocationByUid(duid);

            if (result.hasNext() && result1.hasNext()){
                JSONObject res = new JSONObject();
                
                Record user = result.next();
                Record driver = result1.next();

                Result shortestpath = this.dao.findShortestPath(uid, duid);
                if (!shortestpath.hasNext()){
                    System.out.println("Unable to find distance");
                    return;
                }
                
                JSONObject data = new JSONObject();
                // TODO

                
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
