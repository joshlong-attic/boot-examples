package demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.*;


@ComponentScan
@EnableAutoConfiguration
public class Application extends SpringBootServletInitializer {

    private static Class<Application> entryPointClass = Application.class;

    public static void main(String[] args) {
        SpringApplication.run(entryPointClass, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(entryPointClass);
    }

}

/**
 * Represents one (very under-specified) <em>booking</em>
 * for a named person at a theoretical establishment like a restaurant.
 */
@Entity
class Booking {

    private String bookingName;

    @Id
    @GeneratedValue
    private long id;


    Booking() {
    }

    @Override
    public String toString() {
        return "Booking{" +
                "bookingName='" + bookingName + '\'' +
                ", id=" + id +
                '}';
    }

    public String getBookingName() {
        return bookingName;
    }

    public void setBookingName(String bookingName) {
        this.bookingName = bookingName;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Booking(String bookingName) {
        this.bookingName = bookingName;
    }
}

/**
 * Spring Data JPA-powered <em>repository</em> interface.
 * Supports common operations like {@link #findAll()} and {@link #save(Object)} against JPA entities.
 * This particular repository deals in {@link demo.Booking booking} objects.
 */
interface BookingRepository extends JpaRepository<Booking, Long> {
    Collection<Booking> findByBookingName(String bookingName);
}

/**
 * Handles REST-API calls for {@link demo.Booking booking data}.
 */
@RestController
@RequestMapping("/bookings")
class BookingRestController {

    @Autowired
    BookingRepository bookingRepository;

    @RequestMapping(method = RequestMethod.POST)
    Booking add(@RequestBody Booking b) {
        return this.bookingRepository.save(b);
    }

    @RequestMapping(method = RequestMethod.GET)
    Collection<Booking> all() {
        return this.bookingRepository.findAll();
    }
}

/**
 * Handles the Thymeleaf-powered view responses.
 */
@Controller
class BookingHtmlController {

    @Autowired
    BookingRepository bookingRepository;

    Map<String, Object> mapForProperties(Booking b) {
        Map<String, Object> stringObjectMap = new HashMap<>();
        stringObjectMap.put("id", b.getId());
        stringObjectMap.put("bookingName", b.getBookingName());
        return stringObjectMap;
    }

    List<Map<String, Object>> mapList(List<Booking> bookingList) {
        List<Map<String, Object>> maps = new ArrayList<Map<String, Object>>();
        for (Booking b : bookingList)
            maps.add(mapForProperties(b));
        return maps;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/bookings.html")
    String all(Model model) {
        model.addAttribute("bookings", mapList(
                this.bookingRepository.findAll()));
        return "bookings";
    }
}
