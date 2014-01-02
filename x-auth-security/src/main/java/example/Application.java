package example;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.filter.GenericFilterBean;

@ComponentScan
@EnableAutoConfiguration
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}

/**
 * This controller generates the token that must be present in subsequent REST
 * invocations.
 */
@RestController
class UserTransferController {

	private final TokenUtils tokenUtils;
	private final AuthenticationManager authenticationManager;
	private final UserDetailsService userDetailsService;

	@Autowired
	public UserTransferController(AuthenticationManager am, UserDetailsService userDetailsService, TokenUtils tokenUtils) {
		this.tokenUtils = tokenUtils;
		this.authenticationManager = am;
		this.userDetailsService = userDetailsService;
	}

	@RequestMapping(value = "/auth")
	public UserTransfer authorize(@RequestParam String username, @RequestParam String password) {

		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, password);
		Authentication authentication = this.authenticationManager.authenticate(token);
		SecurityContextHolder.getContext().setAuthentication(authentication);

		UserDetails details = this.userDetailsService.loadUserByUsername(username);

		Map<String, Boolean> roles = new HashMap<String, Boolean>();
		for (GrantedAuthority authority : details.getAuthorities())
			roles.put(authority.toString(), Boolean.TRUE);

		return new UserTransfer(details.getUsername(), roles, tokenUtils.createToken(details));
	}

	public static class UserTransfer {

		private final String name;
		private final Map<String, Boolean> roles;
		private final String token;

		public UserTransfer(String userName, Map<String, Boolean> roles, String token) {

			Map<String, Boolean> mapOfRoles = new ConcurrentHashMap<String, Boolean>();
			for (String k : roles.keySet())
				mapOfRoles.put(k, roles.get(k));

			this.roles = mapOfRoles;
			this.token = token;
			this.name = userName;
		}

		public String getName() {
			return this.name;
		}

		public Map<String, Boolean> getRoles() {
			return this.roles;
		}

		public String getToken() {
			return this.token;
		}
	}
}

@Component
class TokenUtils {

	public static final String MAGIC_KEY = "obfuscate";

	public String createToken(UserDetails userDetails) {
		long expires = System.currentTimeMillis() + 1000L * 60 * 60;
		return userDetails.getUsername() + ":" + expires + ":" + computeSignature(userDetails, expires);
	}

	public String computeSignature(UserDetails userDetails, long expires) {
		StringBuilder signatureBuilder = new StringBuilder();
		signatureBuilder.append(userDetails.getUsername()).append(":");
		signatureBuilder.append(expires).append(":");
		signatureBuilder.append(userDetails.getPassword()).append(":");
		signatureBuilder.append(TokenUtils.MAGIC_KEY);

		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("No MD5 algorithm available!");
		}
		return new String(Hex.encode(digest.digest(signatureBuilder.toString().getBytes())));
	}

	public String getUserNameFromToken(String authToken) {
		if (null == authToken) {
			return null;
		}
		String[] parts = authToken.split(":");
		return parts[0];
	}

	public boolean validateToken(String authToken, UserDetails userDetails) {
		String[] parts = authToken.split(":");
		long expires = Long.parseLong(parts[1]);
		String signature = parts[2];
		String signatureToMatch = computeSignature(userDetails, expires);
		return expires >= System.currentTimeMillis() && signature.equals(signatureToMatch);
	}
}

@EnableWebMvcSecurity
@EnableWebSecurity(debug = false)
@Configuration
@Order
class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private TokenUtils tokenUtils;

	@Override
	protected void configure(HttpSecurity http) throws Exception {

		CustomFilter customFilter = new CustomFilter(tokenUtils, userDetailsServiceBean());

		http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
		http.addFilterBefore(customFilter, UsernamePasswordAuthenticationFilter.class);
		http.authorizeRequests().antMatchers("/" + GreetingController.GREETING_NAME + "/**").hasRole(CustomUserDetailsService.ROLE_USER);
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

class CustomFilter extends GenericFilterBean {

	private UserDetailsService detailsService;
	private TokenUtils tokenUtils;
	private String xAuthTokenHeaderName = "x-auth-token";

	public CustomFilter(TokenUtils tokenUtils, UserDetailsService userDetailsService) {
		this.detailsService = userDetailsService;
		this.tokenUtils = tokenUtils;
	}

	@Override
	public void doFilter(ServletRequest arg0, ServletResponse arg1, FilterChain filterChain) throws IOException, ServletException {
		try {
			HttpServletRequest httpServletRequest = (HttpServletRequest) arg0;
			String authToken = httpServletRequest.getHeader(this.xAuthTokenHeaderName);

			if (StringUtils.hasText(authToken)) {
				String username = this.tokenUtils.getUserNameFromToken(authToken);

				UserDetails details = this.detailsService.loadUserByUsername(username);

				if (this.tokenUtils.validateToken(authToken, details)) {
					UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(details, details.getPassword(), details.getAuthorities());
					SecurityContextHolder.getContext().setAuthentication(token);
				}
			}
			filterChain.doFilter(arg0, arg1);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
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
			return "mikey";
		}

		@Override
		public String getPassword() {
			return "bear";
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

// greeting REST service
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
	public Greeting greet(@PathVariable String name) {
		return new Greeting(name);
	}
}
