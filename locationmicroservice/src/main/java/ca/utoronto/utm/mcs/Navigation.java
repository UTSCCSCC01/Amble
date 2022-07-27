package ca.utoronto.utm.mcs;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        
        String[] params = r.getRequestURI().toString().split("\\?passengerUid=");
        String p_uid;
        String d_uid;
        JSONObject res = new JSONObject();
        JSONObject data = new JSONObject();

        if (params.length != 2 || params[1].isEmpty()) {
            this.sendStatus(r, 400);
            return;
        }
        p_uid = params[1];
        String [] x =  params[0].split("/");
        if(x.length != 4 || x[x.length - 1].isEmpty()){
            this.sendStatus(r, 400);
            return;
        }
        d_uid = x[x.length - 1];

        String d_street = null;
        String p_street = null;
        boolean driver = false;

        Result result = this.dao.findUser(d_uid);
        if (result.hasNext()) {
            Record user = result.next();
            d_street = user.get("n.street").asString();
        }
        result = this.dao.findUser(p_uid);
        if (result.hasNext()) {
            Record user = result.next();
            p_street = user.get("n.street").asString();
        }
        result = this.dao.findUser(d_uid);
        if (result.hasNext()) {
            Record user = result.next();
            driver = user.get("n.is_driver").asBoolean();
        }
        if (!driver || d_street == null || p_street == null){
            this.sendStatus(r, 404);
            return;
        }

        Result path = this.dao.findShortestPath(d_street, p_street);
        List<Record> entire_path = path.list();
        if (entire_path.isEmpty()){
            this.sendStatus(r, 404);
            return;
        }

        List<?> dist = entire_path.get(0).get(1).asList();
        Double total_time = entire_path.get(0).get(0).asDouble();
        List<Double> distances = new ArrayList<>();
        for (Object obj: dist){
            distances.add((double) obj);
        }

        JSONArray route = new JSONArray();
        Value road = entire_path.get(0).get(2);
        String street;
        double time, prv_dist = 0.0;
        boolean has_traffic;
        int i=0;
        while (i <road.asList().size()) {
            JSONObject road_i = new JSONObject();
            
            //Find Values 
            street = road.get(i).get("name").asString();
            has_traffic = road.get(i).get("has_traffic").asBoolean();
            
            // Calculate Time
            time = distances.get(i) - prv_dist;

            // Add to road to route
                road_i.put("street", street);
                road_i.put("time", time);
                road_i.put("has_traffic", has_traffic);
            route.put(road_i);

            prv_dist = distances.get(i);
            i++;
        }

        res.put("status", "OK");
            data.put("total_time", total_time);
            data.put("route", route);
        res.put("data", data);


        this.sendResponse(r, res, 200);

    }
    
}
