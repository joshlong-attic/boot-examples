package example;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@ComponentScan
@EnableAutoConfiguration
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}

class Greeting {

	private String message;

	public Greeting() {
	}

	public Greeting(String msg) {
		this.message = msg;
	}

	public String getMessage() {
		return this.message;
	}

	public void setMessage(String m) {
		this.message = m;
	}
}

@RestController
class GreetingController {

	public static final String GREETING_NAME = "greeting";

	@RequestMapping("/" + GREETING_NAME + "/{name}")
	Greeting greet(@PathVariable String name) {
		return new Greeting(name);
	}

}

@EnableWebMvcSecurity
@EnableWebSecurity(debug = false)
@Configuration
@Order
class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
		http.addFilterBefore(new CustomFilter(userDetailsServiceBean()), org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);
		http.authorizeRequests().antMatchers("/" + GreetingController.GREETING_NAME + "/**").hasRole("USER");
	}

	@Override
	protected void configure(AuthenticationManagerBuilder authManagerBuilder) throws Exception {
		authManagerBuilder.inMemoryAuthentication().withUser("user").password("password").roles("USER").and().withUser("admin").password("password").roles("USER", "ADMIN");
	}

	@Override
	protected UserDetailsService userDetailsService() {
		return new CustomUserDetailsService();
	}

	// Expose the UserDetailsService as a Bean
	@Bean
	@Override
	public UserDetailsService userDetailsServiceBean() throws Exception {
		return super.userDetailsServiceBean();
	}

	@Bean
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}
}

class CustomFilter extends GenericFilterBean {

	private UserDetailsService detailsService;

	public CustomFilter(UserDetailsService userDetailsService) {
		this.detailsService = userDetailsService;
	}

	@Override
	public void doFilter(ServletRequest arg0, ServletResponse arg1, FilterChain filterChain) throws IOException, ServletException {
		try {
			HttpServletRequest httpServletRequest = (HttpServletRequest) arg0;
			String authToken = httpServletRequest.getHeader("x-test");
			
			if (StringUtils.hasText(authToken) && authToken.equalsIgnoreCase("nou")) {
				
				UserDetails userDetails = detailsService.loadUserByUsername("user");

				UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(), userDetails.getAuthorities());
				SecurityContextHolder.getContext().setAuthentication(token);
			}
			
			filterChain.doFilter(arg0, arg1);
			
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

}

class CustomUserDetailsService implements UserDetailsService {
	private UserDetails details = new CustomUserDetails();

	@Override
	public UserDetails loadUserByUsername(String arg0) throws UsernameNotFoundException {
		return this.details;
	}
}

@SuppressWarnings("serial")
class CustomUserDetails implements UserDetails {

	public CustomUserDetails() {
	}

	private boolean enabled = true;

	@Override
	public boolean isEnabled() {
		return this.enabled;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return this.enabled;
	}

	@Override
	public boolean isAccountNonLocked() {
		return this.enabled;
	}

	@Override
	public boolean isAccountNonExpired() {
		return this.enabled;
	}

	@Override
	public String getUsername() {
		return "mikey";
	}

	@Override
	public String getPassword() {
		return "bear";
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		Collection<String> roles = Arrays.<String> asList("user", "admin");
		Collection<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
		for (String r : roles) {
			authorities.add(new SimpleGrantedAuthority(r));
		}
		return authorities;
	}
}