package ca.utoronto.utm.mcs;

import com.sun.net.httpserver.HttpExchange;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.*;

public class Login extends Endpoint {

    /**
     * POST /user/login
     * @body email, password
     * @return 200, 400, 401, 404, 500
     * Login a user into the system if the given information matches the 
     * information of the user in the database.
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

        String[] fields = {"email","password"};
        Class<?>[] fieldClass = {String.class, String.class};

        if(!this.validateFields(deserialized, fields, fieldClass)){
            this.sendStatus(r, 400);
            return;
        }

        String email = deserialized.getString("email");
        String password = deserialized.getString("password");


		// make query to check if email exists, return 404 if not exist
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
        
        if(!resultHasNext){
            this.sendStatus(r, 404);
            return;
        }
      
		//Once we get to here, the follow preconditions are satisfied
        // 1) email,password are string types. 
        // 2) email exists hence account exists

		

        //Pull the uid, and password of the account in question.
        String uid = null;  
        String user_auth_pass = null;  
		try {
			ResultSet res;
            res = this.dao.getUserCred(email);
            if(res.next()){
                uid = res.getString("uid");
                user_auth_pass = res.getString("password");
            }
		}
		catch (SQLException e) {
            e.printStackTrace();
			this.sendStatus(r, 500);
			return;
		}

        if(uid == null || user_auth_pass == null){ 
            this.sendStatus(r, 500);
            return;
        }

        //If password incorrect
        if(!user_auth_pass.equals(password)){
            this.sendStatus(r, 401);
            return;
        }


        //Prepare for response to server
        JSONObject response = new JSONObject();
        response.put("uid", uid);
		// return 200 if everything is updated without error
		this.sendResponse(r,response,200);      
    }
}
