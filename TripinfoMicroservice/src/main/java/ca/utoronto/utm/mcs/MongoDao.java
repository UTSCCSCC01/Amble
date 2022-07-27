package ca.utoronto.utm.mcs;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoException;
import com.mongodb.MongoWriteException;
import com.mongodb.client.*;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import static com.mongodb.client.model.Filters.eq;

import io.github.cdimascio.dotenv.Dotenv;

import java.io.Console;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONException;
import org.json.JSONObject;

public class MongoDao {
	
	public MongoCollection<Document> collection;


	private final String username = "root";
	private final String password = "123456";
	private final String uriDb = String.format("mongodb://%s:%s@%s:27017", username, password, Dotenv.load().get("MONGODB_ADDR"));
	private final String dbName = "trip";

	public MongoDao() {
        // TODO: 
        // Connect to the mongodb database and create the database and collection. 
        // Use Dotenv like in the DAOs of the other microservices.

		MongoClient mongoClient = MongoClients.create(this.uriDb);
        MongoDatabase database = mongoClient.getDatabase(this.dbName);
        this.collection = database.getCollection(this.dbName);
	}

	// *** implement database operations here *** //
	public JSONObject addTrip(String driver, String passenger, int startTime) throws JSONException, MongoWriteException, MongoException, Exception{
		System.out.println("created the document");
		Document doc = new Document();
        doc.put("driver", driver);
        doc.put("passenger", passenger);
        doc.put("startTime", startTime);
		System.out.println("trying to insert");
		// Convert insert document into a json.
        this.collection.insertOne(doc);
		String rep = doc.toJson().toString();
		JSONObject result = new JSONObject(rep);

		System.out.println("Creating fields");
		// Create the id field
		JSONObject idField = new JSONObject();
		idField.put("_id", result.get("_id"));

		// Create the final payload
		JSONObject data = new JSONObject();
		data.put("data", idField);

		return data;
	}

	public Document getTrip(String tripid) throws MongoWriteException, MongoException, Exception{
		try {
            Document trip = this.collection.find(eq("_id", new ObjectId(tripid))).first();
			return trip;
		} catch (Exception e) {
            ;
			//exception
        }
        return null;
	}


	public FindIterable<Document>getUserTrip(String userid) throws MongoWriteException, MongoException, Exception{
		try {
            FindIterable<Document> documents = this.collection.find(eq("passenger", userid));
			return documents;
		} catch (Exception e) {
            ;
			//exception
        }
        return null;
	}

	public FindIterable<Document>getDriverTrip(String driverid) throws MongoWriteException, MongoException, Exception{
		try {
            FindIterable<Document> documents = this.collection.find(eq("driver", driverid));
			return documents;
		} catch (Exception e) {
            ;
			//exception
        }
        return null;
	}




	public boolean updateTrip(String tripid, int distance, int endTime, int timeElapsed, String totalcost){

		//Precondition tripid exists. Assume no race condition with an object deleting.
		Document doc = new Document();
        doc.put("distance", distance);
        doc.put("endTime", endTime);
        doc.put("timeElapsed", timeElapsed);
		doc.put("totalCost", totalcost);
		try{
			this.collection.updateOne(eq("_id", new ObjectId(tripid)), new Document("$set", doc));
			return true;
		}catch(Exception e){
			e.printStackTrace();
		}
		return false;
	}
	

}
