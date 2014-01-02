package example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import example.xauth.XAuthTokenConfigurer;

@ComponentScan
@EnableAutoConfiguration
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}

@EnableWebMvcSecurity
@EnableWebSecurity(debug = false)
@Configuration
@Order
class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		
		
		http.csrf().disable();
		http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

		String[] restEndpointsToSecure = { NewsController.NEWS_COLLECTION };
		for (String endpoint : restEndpointsToSecure) {
			http.authorizeRequests().antMatchers("/" + endpoint + "/**").hasRole(CustomUserDetailsService.ROLE_USER);
		}
		
		http.apply(new XAuthTokenConfigurer(userDetailsServiceBean()));
	}

	@Override
	protected void configure(AuthenticationManagerBuilder authManagerBuilder) throws Exception {
		authManagerBuilder.userDetailsService(new CustomUserDetailsService());
	}

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

class CustomUserDetailsService implements UserDetailsService {

	public static final String ROLE_ADMIN = "ADMIN";
	public static final String ROLE_USER = "USER";

	@SuppressWarnings("serial")
	private UserDetails details = new UserDetails() {
		private boolean enabled = true;

		public String toString() {
			return "{enabled:" + isEnabled() + ", username:'" + getUsername() + "', password:'" + getPassword() + "'}";
		}

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
			return "user";
		}

		@Override
		public String getPassword() {
			return "password";
		}

		private String role(String i) {
			return "ROLE_" + i;
		}

		@Override
		public Collection<? extends GrantedAuthority> getAuthorities() {
			Collection<String> roles = Arrays.<String> asList(role(ROLE_USER), role(ROLE_ADMIN));
			Collection<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
			for (String r : roles) {
				authorities.add(new SimpleGrantedAuthority(r));
			}
			return authorities;
		}

	};

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		if (username.equalsIgnoreCase(this.details.getUsername())) {
			return this.details;
		}
		return null;
	}
}

@RestController
class NewsController {
	private Map<Long, NewsEntry> entries = new ConcurrentHashMap<Long, NewsEntry>();

	public static final String NEWS_COLLECTION = "news";

	@RequestMapping("/" + NEWS_COLLECTION)
	public Collection<NewsEntry> entries() {
		return this.entries.values();
	}

	@RequestMapping("/" + NEWS_COLLECTION + "/{id}")
	public NewsEntry entry(@PathVariable Long id) {
		return this.entries.get(id);
	}

	public NewsController() {
		for (long i = 0; i < 5; i++)
			this.entries.put(i, new NewsEntry(i, "Title #" + i));
	}

	public static class NewsEntry {
		private long id;
		private String content;

		public NewsEntry(long id, String b) {
			this.id = id;
			this.content = b;
		}

		public long getId() {
			return this.id;
		}

		public String getContent() {
			return this.content;
		}
	}

}
