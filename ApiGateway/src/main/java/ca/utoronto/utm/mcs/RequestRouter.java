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
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.HashMap;
import java.net.URI;
import java.io.OutputStream;    // Also given to you to send back your response

public class RequestRouter implements HttpHandler {
	
    /**
     * You may add and/or initialize attributes here if you 
     * need.
     */
        //Maps the path to applicable host
        // /location -> location microservice
        // /
        HashMap<String, String> context_map;
        HttpClient http_client;


	public RequestRouter() {
                http_client = HttpClient.newHttpClient();
                context_map = new HashMap<String, String>();

                context_map.put("location", "http://locationmicroservice:8000");
                context_map.put("user", "http://usermicroservice:8000");
                context_map.put("trip", "http://tripinfomicroservice:8000");
	}
   

        /* Creates a HttpRequest object that is to be directed to the context mapped microservice*/
        public HttpRequest requestBuilder(HttpExchange request, String context) throws IOException, InterruptedException, IllegalStateException{
                //Gather information required to build a new request object
                Builder req = HttpRequest.newBuilder();
                BodyPublisher body = BodyPublishers.ofByteArray(request.getRequestBody().readAllBytes());
                String uri_gen = context_map.get(context) + request.getRequestURI().toString();
                //Build request object
                req.uri(URI.create(uri_gen)); //set uri
                req.method(request.getRequestMethod(), body);
                HttpRequest indexed_req = req.build();
                return indexed_req;
        }

        public void sendResponse(HttpRequest request, HttpExchange r) throws IOException, InterruptedException{
                /*Takes a http request to the microservice, get its responseObject, and and sends it to the 
                 * the original caller. 
                */
                HttpResponse<byte[]> response = http_client.send(request, BodyHandlers.ofByteArray());
                r.sendResponseHeaders(response.statusCode(),response.body().length);
                OutputStream os = r.getResponseBody();
                os.write(response.body());
                os.close();
                return;
        }

        public void handleError(HttpExchange r, String payload, int errorCode) throws IOException{
                r.sendResponseHeaders(errorCode, payload.length());
                OutputStream os = r.getResponseBody();
                os.write(payload.getBytes());
                os.close();
                return;
        }

	@Override
	public void handle(HttpExchange r) throws IOException {
                String[] tokens = r.getRequestURI().getPath().replaceFirst("/", "").split("/");
                String url_context = tokens[0];
                System.out.println(url_context);

                if(context_map.containsKey(url_context)){
                        try{
                                HttpRequest request = requestBuilder(r, url_context);
                                sendResponse(request, r);
                                return;
                        }catch(Exception e){    
                                System.out.println("Error has occured");
                                //Send Error intenal server.
                                handleError(r,"An internal server error has occured",500);
                                return;
                        }
                }

                handleError(r, "Not Found", 404);
	}
}
