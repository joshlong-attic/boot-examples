package example;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
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
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.context.AbstractSecurityWebApplicationInitializer;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@ComponentScan
@EnableAutoConfiguration
public class Application extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(Application.class);
	}

	public static void main(String[] args) {
		ApplicationContext applicationContext = SpringApplication.run(Application.class, args);
		applicationContext.getBean(DataBaseInitializer.class).init();
	}
}

@EnableSpringDataWebSupport
@EnableTransactionManagement
@Configuration
class CommonConfiguration {

	@Bean
	public SaltedSHA256PasswordEncoder saltedSHA256PasswordEncoder(Environment environment) throws NoSuchAlgorithmException {
		return new SaltedSHA256PasswordEncoder(environment.getProperty("secret"));
	}
}

@Configuration
@EnableWebSecurity(debug = true)
// @Order(Ordered.LOWEST_PRECEDENCE - 8)
class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private SaltedSHA256PasswordEncoder saltedSHA256PasswordEncoder;

	@Autowired
	private UserDetailsService userDetailsService;

	@Autowired
	private TokenUtils tokenUtils;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		// disable CSRF and Basic Authentication
		http.csrf().disable();
		http.httpBasic().disable();

		// level setting for now
		http.headers().contentTypeOptions().disable();
		http.headers().frameOptions().disable();
		http.headers().httpStrictTransportSecurity().disable();
		http.headers().xssProtection().disable();
		http.headers().cacheControl().disable();
		
//		http.exceptionHandling().defaultAuthenticationEntryPointFor(entryPoint, preferredMatcher)

		String user = "user", admin = "admin", restNewsPattern = "/rest/news*/**", restAuthPattern = "/rest/user/authenticate";

		// @formatter:on
		http.authorizeRequests().antMatchers(restAuthPattern).permitAll().antMatchers(HttpMethod.GET, restNewsPattern).hasRole(user).antMatchers(HttpMethod.PUT, restNewsPattern).hasRole(admin)
				.antMatchers(HttpMethod.POST, restNewsPattern).hasRole(admin).antMatchers(HttpMethod.DELETE, restNewsPattern).hasRole(admin) ; 
		
		http.apply(new XAuthTokenConfigurer(this.tokenUtils));
		 
		// @formatter:off

		// http.apply(new XAuthTokenConfigurer(this.tokenUtils));
		// install custom X-Auth-Token support

	}

	@Override
	protected void configure(AuthenticationManagerBuilder authManagerBuilder) throws Exception {
		authManagerBuilder.userDetailsService(userDetailsService)
						  .passwordEncoder(saltedSHA256PasswordEncoder);
	}

	@Bean
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}
}

class XAuthTokenProcessingFilter extends AbstractAuthenticationProcessingFilter {

	private final UserDetailsService userService;
	private final TokenUtils tokenUtils;

	protected XAuthTokenProcessingFilter(String defaultFilterProcessesUrl, TokenUtils tokenUtils, AuthenticationManager authenticationManager, UserDetailsService userService) {
		super(defaultFilterProcessesUrl);
		setAuthenticationManager(authenticationManager);
		this.tokenUtils = tokenUtils;
		this.userService = userService;
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
		logger.info("request to " + ((HttpServletRequest) request).getRequestURI());

		HttpServletRequest httpRequest = (HttpServletRequest) request;
		String authToken = httpRequest.getHeader("X-Auth-Token");
		String userName = tokenUtils.getUserNameFromToken(authToken);

		if (userName != null) {

			UserDetails userDetails = this.userService.loadUserByUsername(userName);

			if (tokenUtils.validateToken(authToken, userDetails)) {
				UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
				authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails((HttpServletRequest) request));
				return authentication;
			}
		}

		throw new InsufficientAuthenticationException("couldn't find the token corresponding to the");
	}
}

class XAuthTokenConfigurer extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {

	public XAuthTokenConfigurer(TokenUtils tokenUtils) {
		this.tokenUtils = tokenUtils;
	}

	private XAuthTokenProcessingFilter authTokenProcessingFilter;
	private TokenUtils tokenUtils;
	private AuthenticationManager authenticationManager;
	private UserDetailsService userDetailsService;

	@Override
	public void init(HttpSecurity builder) throws Exception {
	}

	@Override
	public void configure(HttpSecurity builder) throws Exception {
		/*
		 * <security:http realm="Protected API" use-expressions="true"
		 * auto-config="false" create-session="stateless"
		 * entry-point-ref="unauthorizedEntryPoint"
		 * authentication-manager-ref="authenticationManager">
		 * <security:custom-filter ref="authenticationTokenProcessingFilter"
		 * position="FORM_LOGIN_FILTER" /> <security:intercept-url
		 * pattern="/rest/user/authenticate" access="permitAll" />
		 * <security:intercept-url method="GET" pattern="/rest/news/**"
		 * access="hasRole('user')" /> <security:intercept-url method="PUT"
		 * pattern="/rest/news/**" access="hasRole('admin')" />
		 * <security:intercept-url method="POST" pattern="/rest/news/**"
		 * access="hasRole('admin')" /> <security:intercept-url method="DELETE"
		 * pattern="/rest/news/**" access="hasRole('admin')" /> </security:http>
		 */

		this.userDetailsService = builder.getSharedObject(UserDetailsService.class);
		this.authenticationManager = builder.getSharedObject(AuthenticationManager.class);

		// register x-auth token filter
		this.authTokenProcessingFilter = new XAuthTokenProcessingFilter("/rest/*", this.tokenUtils, this.authenticationManager, this.userDetailsService);
		postProcess(this.authTokenProcessingFilter);

		//builder.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

		builder.addFilterAfter(this.authTokenProcessingFilter, UsernamePasswordAuthenticationFilter.class); 

	}

}