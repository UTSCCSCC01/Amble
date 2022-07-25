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
                
                JSONObject data = new JSONObject();
                Result shortestpath = this.dao.findShortestPath(uid, duid);
                
                int total_time = 0;

                
                while (shortestpath.hasNext()){
                    Record roads = result.next();
                    // ! check if its the name
                    String street = roads.get("n.name").asString();
                    Boolean has_traffic = roads.get("n.has_traffic").asBoolean();
                    
                    // Boolean travel_time = roads.get("n.travel_time").asBoolean();

                    JSONObject route = new JSONObject();
                    route.put("street", street);
                    route.put("has_traffic", has_traffic);
                    // route.put("time", travel_time);
                }

                // Add route and time to data
                data.put("total_time", total_time); 
                // data.put("route", route);
                
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
