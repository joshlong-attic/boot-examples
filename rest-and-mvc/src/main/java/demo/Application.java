package demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Collection;

@ComponentScan
@EnableAutoConfiguration
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

@RestController
class BookingRestController {

    @Autowired
    BookingRepository bookingRepository;

    @RequestMapping("/bookings")
    Collection<Booking> bookings() {
        return this.bookingRepository.findAll();
    }
}

@Controller
class BookingHtmlController {

    @Autowired
    BookingRepository bookingRepository;

    @RequestMapping("/bookings.html")
    String bookings(Model model) {
        model.addAttribute("bookings", this.bookingRepository.findAll());
        return "bookings";
    }
}

@Entity
class Booking {

    @Column(name = "booking_name")
    private String bookingName;

    @Id
    @GeneratedValue
    private long id;

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

    public Booking() {
    }

    public Booking(String bookingName) {
        this.bookingName = bookingName;
    }
}


interface BookingRepository extends JpaRepository<Booking, Long> {
    Collection<Booking> findByBookingName(@Param("bookingName") String bookingName);
}


@Configuration
@EnableSpringDataWebSupport
@EnableTransactionManagement
class BookingConfiguration {
}