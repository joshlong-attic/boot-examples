package demo;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/greetings")
public class GreetingEndpoint {
    @Produces({"application/json", "application/xml"})
    @GET
    public Message message() {
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

