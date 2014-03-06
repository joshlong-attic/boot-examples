# Rest and MVC example on JBoss Undertow  

This branch demonstrates how to run a Spring Boot application on JBoss Undertow. It required only that I remove _provided_ modules. This is the Maven build I used to make this simple Spring Boot and Spring 4-powered application work on JBoss' spiffy Undertow AS server. Then, i dragged the built `.war` into the `$JBOSS_HOME/standalone/deployments` directory. I was able to get [the REST resource from the app-context relative URI](http://localhost:8080/rest-and-mvc-1.0.0.RC4/bookings).
