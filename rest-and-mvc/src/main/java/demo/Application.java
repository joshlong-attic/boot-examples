package demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Collection;

@ComponentScan
@EnableAutoConfiguration
public class Application extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(Application.class);
    }

    public static void main(String[] args) {
        ApplicationContext applicationContext =
                SpringApplication.run(Application.class, args);

    }
}

@Entity
class Booking {
    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "booking_name")
    private String bookingName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBookingName() {
        return bookingName;
    }

    public void setBookingName(String bookingName) {
        this.bookingName = bookingName;
    }

    public Booking() {
    }

    public Booking(String bookingName) {

        this.bookingName = bookingName;
    }
}

@RestController
class BookingRestController {
    @Autowired
    BookingRepository bookingRepository;

    @RequestMapping("/bookings")
    Collection<Booking> allBookings() {
        return bookingRepository.findAll();
    }


}

@Controller
class BookingMvcController {

    @RequestMapping("/bookings.html")
    String allBookings(Model model) {
        model.addAttribute("bookings", bookingRepository.findAll());
        return "bookings";
    }

    @Autowired
    BookingRepository bookingRepository;
}

interface BookingRepository extends JpaRepository<Booking, Long> {
    // select b from Booking b where b.bookingName = ?
    Collection<Booking> findByBookingName(@Param("bookingName") String bookingName);

}


@Configuration
@EnableSpringDataWebSupport
@EnableTransactionManagement
class BookingConfiguration {
}