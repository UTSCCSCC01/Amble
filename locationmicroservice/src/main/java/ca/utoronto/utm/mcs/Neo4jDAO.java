package ca.utoronto.utm.mcs;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.neo4j.driver.*;
import java.util.*;
import io.github.cdimascio.dotenv.Dotenv;
import org.neo4j.driver.Record;
import org.neo4j.driver.types.*;

public class Neo4jDAO {

    private final Session session;
    private final Driver driver;
    private final String username = "neo4j";
    private final String password = "123456";

    public Neo4jDAO() {
        Dotenv dotenv = Dotenv.load();
        String addr = dotenv.get("NEO4J_ADDR");
        String uriDb = "bolt://" + addr + ":7687";

        this.driver = GraphDatabase.driver(uriDb, AuthTokens.basic(this.username, this.password));
        this.session = this.driver.session();
    }

    // *** implement database operations here *** //

    public Result addUser(String uid, boolean is_driver) {
        String query = "CREATE (n: user {uid: '%s', is_driver: %b, longitude: 0, latitude: 0, street: ''}) RETURN n";
        query = String.format(query, uid, is_driver);
        return this.session.run(query);
    }

    public Result deleteUser(String uid) {
        String query = "MATCH (n: user {uid: '%s' }) DETACH DELETE n RETURN n";
        query = String.format(query, uid);
        return this.session.run(query);
    }

    public Result getUserLocationByUid(String uid) {
        String query = "MATCH (n: user {uid: '%s' }) RETURN n.longitude, n.latitude, n.street";
        query = String.format(query, uid);
        return this.session.run(query);
    }

    public Result getUserByUid(String uid) {
        String query = "MATCH (n: user {uid: '%s' }) RETURN n";
        query = String.format(query, uid);
        return this.session.run(query);
    }

    public Result updateUserIsDriver(String uid, boolean isDriver) {
        String query = "MATCH (n:user {uid: '%s'}) SET n.is_driver = %b RETURN n";
        query = String.format(query, uid, isDriver);
        return this.session.run(query);
    }

    public Result updateUserLocation(String uid, double longitude, double latitude, String street) {
        String query = "MATCH(n: user {uid: '%s'}) SET n.longitude = %f, n.latitude = %f, n.street = \"%s\" RETURN n";
        query = String.format(query, uid, longitude, latitude, street);
        return this.session.run(query);
    }

    public Result getRoad(String roadName) {
        String query = "MATCH (n :road) where n.name='%s' RETURN n";
        query = String.format(query, roadName);
        return this.session.run(query);
    }

    public Result createRoad(String roadName, boolean has_traffic) {
        String query = "CREATE (n: road {name: '%s', has_traffic: %b}) RETURN n";
        query = String.format(query, roadName, has_traffic);
        return this.session.run(query);
    }

    public Result updateRoad(String roadName, boolean has_traffic) {
        String query = "MATCH (n:road {name: '%s'}) SET n.has_traffic = %b RETURN n";
        query = String.format(query, roadName, has_traffic);
        return this.session.run(query);
    }

    public Result createRoute(String roadname1, String roadname2, int travel_time, boolean has_traffic) {
        String query = "MATCH (r1:road {name: '%s'}), (r2:road {name: '%s'}) CREATE (r1) -[r:ROUTE_TO {travel_time: %d, has_traffic: %b}]->(r2) RETURN type(r)";
        query = String.format(query, roadname1, roadname2, travel_time, has_traffic);
        return this.session.run(query);
    }

    public Result deleteRoute(String roadname1, String roadname2) {
        String query = "MATCH (r1:road {name: '%s'})-[r:ROUTE_TO]->(r2:road {name: '%s'}) DELETE r RETURN COUNT(r) AS numDeletedRoutes";
        query = String.format(query, roadname1, roadname2);
        return this.session.run(query);
    }
    
    public Result findAllDrivers(){
        String query = "MATCH (d: user {is_driver: true }) RETURN d";
        return this.session.run(query);
    }
    
    public Result findDistance(double user_pt1, double user_pt2, double driver_pt1, double driver_pt2){
        String query = "WITH point({longitude: %f, latitude:  %f, height: 100}) as p1, point({longitude:  %f, latitude:  %f, height: 100}) as p2 RETURN point.distance(p1,p2) as dist";
        query = String.format(query, user_pt1, user_pt2, driver_pt1, driver_pt2 );
        return this.session.run(query);
    }

    public Result findUser(String uid) {
        String query = "MATCH (n: user {uid: '%s' }) RETURN n.longitude, n.latitude, n.street, n.is_driver";
        query = String.format(query, uid);
        return this.session.run(query);
    }

    // public Result findDriversWithinRadius(String uid, double radius){
    //     // String query = "MATCH (t:user {uid: '%s'}), (o: user{is_driver:true}) WHERE point.distance(point({longitude: t.longitude, latitude: t.latitude}), point({longitude: o.longitude, latitude: o.latitude})) < %f Return (o)";
    //     String query = "MATCH (t:user {uid: '%s'}) WITH t MATCH (o: user{is_driver:true}) WHERE point.distance(point({longitude: t.longitude, latitude: t.latitude}), point({longitude: o.longitude, latitude: o.latitude})) < %f Return (o)";
    //     query = String.format(query, uid, radius);
    //     return this.session.run(query);
    // }


    // public Result getgraph(){
    //     String query = "RETURN gds.graph.exists('streetGraph') AS graphExists";
    //     Result graphExistsResult = this.session.run(query);
    //     return graphExistsResult;
    // }
    // public Result graphName()
        
    public Result findShortestPath(String user_road, String driver_road){
        String query = """
                        MATCH (source:road {name: '%s'}), (target:road {name: '%s'})
                        CALL gds.shortestPath.dijkstra.stream({
                            nodeProjection: 'road',
                            relationshipProjection: 'ROUTE_TO',
                            relationshipProperties: 'travel_time',
                            sourceNode: source,
                            targetNode: target,
                            relationshipWeightProperty: 'travel_time'
                        })
                        YIELD index, sourceNode, targetNode, totalCost, nodeIds, costs, path
                        RETURN
                            totalCost,
                            costs,
                            nodes(path) as path
                        ORDER BY index
                """;
        query = String.format(query, user_road, driver_road);
        return this.session.run(query);
    }


} 