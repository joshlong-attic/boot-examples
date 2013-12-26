package demo;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.view.velocity.VelocityConfig;
import org.springframework.web.servlet.view.velocity.VelocityConfigurer;
import org.springframework.web.servlet.view.velocity.VelocityViewResolver;

@ComponentScan
@EnableAutoConfiguration
public class Application extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(Application.class);
	}

	public static void main(String[] args) {
		SpringApplication.run(Application.class);
	}
}

@Configuration 
@EnableWebMvc
class VelocityMvcConfiguration {
	@Bean
	public VelocityConfigurer velocityConfig() {
		return new VelocityConfigurer();
	}

	@Bean
	public ViewResolver viewResolver() {
		VelocityViewResolver resolver = new VelocityViewResolver();
		resolver.setPrefix("/templates/");
		resolver.setSuffix(".vm");
		return resolver;
	}
}

class Booking {
	public Booking(long i, String n) {
		this.id = i;
		this.name = n;
	}

	private String name;
	private long id;

	public String getName() {
		return name;
	}

	public long getId() {
		return id;
	}

}

@Controller
class BookingMvcController {

	private static List<Booking> bookings(int count) {
		List<Booking> bookings = new ArrayList<Booking>();
		for (int i = 0; i < count; i++)
			bookings.add(new Booking(i, "Booking #" + i));
		return bookings;
	}

	@RequestMapping("/bookings.html")
	String allBookings(Model model) {
		model.addAttribute("bookings", bookings(4));
		return "bookings";
	}

}
