package demo;

import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Collection;
import java.util.Date;

@ComponentScan
@EnableAutoConfiguration
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}


@EnableScheduling
@EnableWebSocketMessageBroker
@Configuration
class WebSocketConfiguration
        extends AbstractWebSocketMessageBrokerConfigurer
        implements SchedulingConfigurer {

    @Bean
    ThreadPoolTaskScheduler reservationPool() {
        return new ThreadPoolTaskScheduler();
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/notifications").withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
    }

    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        registration.taskExecutor().corePoolSize(4).maxPoolSize(10);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/queue/", "/topic/");
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setTaskScheduler(reservationPool());
    }
}


@Entity
class Booking {

    @Id
    @GeneratedValue
    private Long id;

    private int groupSize = 1;
    private Date dateAndTime;
    private String bookingName;

    @Override
    public String toString() {
        return "Reservation{" +
                "groupSize=" + groupSize +
                ", dateAndTime=" + dateAndTime +
                ", id=" + id +
                ", bookingName='" + bookingName + '\'' +
                '}';
    }

    public int getGroupSize() {
        return groupSize;
    }

    public Date getDateAndTime() {
        return dateAndTime;
    }

    public Long getId() {
        return id;
    }

    public String getBookingName() {
        return bookingName;
    }

}


/**
 * Spring Data JPA-powered <em>repository</em> interface.
 * Supports common operations like {@link #findAll()} and {@link #save(Object)} against JPA entities.
 * This particular repository deals in {@link demo.Booking booking} objects.
 */
interface BookingRepository extends JpaRepository<Booking, Long> {
    Collection<Booking> findByBookingName(@Param("bookingName") String bookingName);
}

/**
 * Handles REST-API calls for {@link demo.Booking booking data}.
 */
@RestController
@RequestMapping("/bookings")
class BookingRestController {

    private SimpMessageSendingOperations messagingTemplate;

    private TaskScheduler taskScheduler;

    private BookingRepository bookingRepository;

    @Autowired
    BookingRestController(
            @Qualifier("reservationPool") TaskScheduler taskScheduler,
            BookingRepository bookingRepository,
            SimpMessageSendingOperations messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
        this.taskScheduler = (taskScheduler);
        this.bookingRepository = bookingRepository;
    }

    void schedule(final Booking booking) {
        Assert.notNull(booking.getDateAndTime());
        this.taskScheduler.schedule(new Runnable() {
            @Override
            public void run() {
                messagingTemplate.convertAndSend("/topic/alarms", booking);
            }
        }, booking.getDateAndTime());
        LogFactory.getLog(getClass()).info("at " + new Date(
                System.currentTimeMillis()) + "# scheduling "
                + booking.getId() + " for " + booking.getDateAndTime());
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/{id}")
    void delete(@PathVariable Long id) {
        this.bookingRepository.delete(id);
        reservationEvents("delete#" + id);
    }

    protected void reservationEvents(String event) {
        messagingTemplate.convertAndSend("/topic/reservationEvents", event);
    }

    @RequestMapping(method = RequestMethod.POST)
    Booking add(@RequestBody Booking b) {
        Booking result = this.bookingRepository.save(b);
        schedule(b);
        reservationEvents("add#" + b.getId());
        return result;
    }

    @RequestMapping(method = RequestMethod.GET)
    Collection<Booking> all() {
        return this.bookingRepository.findAll(
                new Sort(Sort.Direction.ASC, "dateAndTime"));
    }
}

/**
 * Handles the Thymeleaf-powered view responses.
 */
@Controller
class BookingHtmlController {

    @Autowired
    BookingRepository bookingRepository;

    @RequestMapping(method = RequestMethod.GET, value = "/bookings.html")
    String all(Model model) {
        model.addAttribute("bookings", this.bookingRepository.findAll());
        return "bookings";
    }
}

