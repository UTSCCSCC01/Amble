package ca.utoronto.utm.mcs;

import java.io.IOException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/** 
 * Everything you need in order to send and recieve httprequests to 
 * the microservices is given here. Do not use anything else to send 
 * and/or recieve http requests from other microservices. Any other 
 * imports are fine.
 */
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.io.OutputStream;    // Also given to you to send back your response

public class RequestRouter implements HttpHandler {
	
    /**
     * You may add and/or initialize attributes here if you 
     * need.
     */
	public RequestRouter() {

	}

	@Override
	public void handle(HttpExchange r) throws IOException {
                try {
                        String uri = r.getRequestURI().toString();
                        String[] uriParts = uri.split("/");
                        if (uriParts[0].equals("") && uriParts[1].equals("location") ){
                                  switch(r.getRequestMethod()){
                                        case "GET":
                                                if(uriParts[2].equals(":uid")){
                                                        String response = "payload";
                                                        r.sendResponseHeaders(200, response.length());
                                                        OutputStream os = r.getResponseBody();
                                                        os.write(response.getBytes());
                                                        os.close();
                                                }
                                                if(uriParts[2].equals("nearbyDriver")){

                                                }
                                                if(uriParts[2].equals("navigation")){

                                                }
                                                System.out.println("Get the request");
                                        case "PUT":
                                                if(uriParts[2].equals("user")){

                                                }
                                                if(uriParts[2].equals("road")){

                                                }
                                        case "POST":
                                                if(uriParts[2].equals("hasRoute")){

                                                }
                                        case "PATCH":
                                                if(uriParts[2].equals(":uid")){

                                                }
                                        case "DELETE":
                                                if(uriParts[2].equals("user")){

                                                }
                                                if(uriParts[2].equals("route")){

                                                }
                                        default:
                                                r.sendResponseHeaders(500, -1);
                                                return; 

                                  }      
                        }
                        else if (uriParts[0].equals("") && uriParts[1].equals("trip") ){
                                  switch(r.getRequestMethod()){
                                        case "GET":
                                                if(uriParts[2].equals("passenger")){

                                                }
                                                if(uriParts[2].equals("driver")){

                                                }
                                                if(uriParts[2].equals("driverTime")){

                                                }
                                        case "POST":
                                                if(uriParts[2].equals("confirm")){

                                                }
                                                if(uriParts[2].equals("request")){

                                                }
                                        case "PATCH":
                                                if(uriParts[2].equals(":_id")){

                                                }
                                        default:
                                                r.sendResponseHeaders(500, -1);
                                                return; 


                                  }      
                        }
                        else if (uriParts[0].equals("") && uriParts[1].equals("user") ){
                                  switch(r.getRequestMethod()){
                                        case "GET":
                                                if(uriParts[2].equals(":uid")){

                                                }
                                        case "POST":
                                                if(uriParts[2].equals("register")){

                                                }
                                                if(uriParts[2].equals("login")){

                                                }
                                        case "PATCH":
                                                if(uriParts[2].equals(":uid")){

                                                }
                                        default:
                                                r.sendResponseHeaders(500, -1);
                                                return; 

                                  }      
                        }
                        else{
                                r.sendResponseHeaders(500, -1);
                                return;  
                        }
                }catch(Exception e){
                        r.sendResponseHeaders(500, -1);
                        e.printStackTrace();
                }
        }
}
