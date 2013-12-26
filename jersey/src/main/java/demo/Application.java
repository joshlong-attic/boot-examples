package demo;

import java.util.HashSet;
import java.util.Set;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.boot.web.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import com.sun.jersey.spi.container.servlet.ServletContainer;

@ComponentScan
@EnableAutoConfiguration
public class Application extends SpringBootServletInitializer {

	@Bean
	public ServletRegistrationBean jerseyServlet() {
		ServletRegistrationBean registration = new ServletRegistrationBean(new ServletContainer(), "/*");
		registration.addInitParameter(ServletContainer.APPLICATION_CONFIG_CLASS, JaxRsApplication.class.getName());
		return registration;
	}

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(Application.class);
	}

	public static class JaxRsApplication extends javax.ws.rs.core.Application {
		@Override
		public Set<Class<?>> getClasses() {
			Set<Class<?>> s = new HashSet<Class<?>>();
			s.add(Endpoint.class);
			return s;
		}
	}
}
