package demo;

import jersey.repackaged.com.google.common.collect.Maps;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.util.Map;

@Path("/greetings")
public class GreetingEndpoint {
    /*@GET
    public Map<String,Object> message() {
        Map<String,Object>  stringObjectMap =
                Maps.newHashMapWithExpectedSize(1) ;
        stringObjectMap.put("message", "Hello, from Jersey!");
        return stringObjectMap;

        //return "Hello World from a Jersey-powered endpoint";
    }*/

    @GET public Message message (){
        return new Message("Hello world from a Jersey-powered endpoint!");
    }
}

class Message {
    private String message;

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "Message{" +
                "message='" + message + '\'' +
                '}';
    }

    public Message() {
    }

    Message(String message) {
        this.message = message;
    }
}

