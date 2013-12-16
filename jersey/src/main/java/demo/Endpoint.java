package demo;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/hello")
public class Endpoint {
    @GET
    public String message() {
        return "Hello World from a Jersey-powered endpoint";
    }
}
