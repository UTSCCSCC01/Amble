package ca.utoronto.utm.mcs;

import com.sun.net.httpserver.HttpExchange;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.*;

public class Register extends Endpoint {


    public void sendResponse(HttpExchange r, JSONObject obj, int statusCode) throws JSONException, IOException {
        super.sendResponse(r, obj, statusCode);
    }

    public void sendStatus(HttpExchange r, int statusCode) throws JSONException, IOException {
        super.sendStatus(r, statusCode);
    }

    public boolean validateFields(JSONObject JSONRequest, String[] fields, Class<?>[] fieldClasses) {
        return super.validateFields(JSONRequest, fields, fieldClasses);
    }

    /**
     * POST /user/register
     * @body name, email, password
     * @return 200, 400, 500
     * Register a user into the system using the given information.
     */

    @Override
    public void handlePost(HttpExchange r) throws IOException, JSONException {
     
       
		// check if request url isn't malformed
		String[] splitUrl = r.getRequestURI().getPath().split("/");
		if (splitUrl.length != 3) {
			this.sendStatus(r, 400);
			return;
		}

		// check if name, email, password given and of string, return 400 if not
        String body;
		JSONObject deserialized;
        try{
            body = Utils.convert(r.getRequestBody());
		    deserialized = new JSONObject(body);
        }catch(Exception e){
            this.sendStatus(r, 400);
            return;
        }

        String[] fields = {"name","email","password"};
        Class<?>[] fieldClass = {String.class, String.class, String.class};

        if(!this.validateFields(deserialized, fields, fieldClass)){
            this.sendStatus(r, 400);
            return;
        }

        String name = deserialized.getString("name");
        String email = deserialized.getString("email");
        String password = deserialized.getString("password");


		// make query to check if email exists, return 409 if exists
		ResultSet rs1;
		boolean resultHasNext;
		try {
			rs1 = this.dao.getUserCred(email);
            resultHasNext = rs1.next();
		} 
		catch (SQLException e) {
            e.printStackTrace();
			this.sendStatus(r, 500);
			return;
		}
        
        if(resultHasNext){
            this.sendStatus(r, 409);
            return;
        }
      
		//Once we get to here, the follow preconditions are satisfied
        // 1) name,email,password are string types. 
        // 2) email does not exist. 

		// update db, return 500 if error

        //Attempt to add to database, and then pull uid info
        String uid = null;    
		try {
			ResultSet res = this.dao.addUser(email, name, password);
            //Assume sync op
            res = this.dao.getUserCred(email);
            if(res.next()){
                uid = res.getString("uid");
            }
		}
		catch (SQLException e) {
            e.printStackTrace();
			this.sendStatus(r, 500);
			return;
		}

        if(uid == null){ 
            this.sendStatus(r, 500);
        }
        
        //Prepare for response to server
        JSONObject response = new JSONObject();
        response.put("uid", uid);
		// return 200 if everything is updated without error
		this.sendResponse(r,response,200);
    }
}
