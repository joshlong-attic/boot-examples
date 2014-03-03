package demo.xauth;

import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * @author Philip W. Sorst (philip@sorst.net)
 * @author Josh Long (josh@joshlong.com)
 */
public class XAuthTokenConfigurer extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {

	private UserDetailsService detailsService;

	public XAuthTokenConfigurer(UserDetailsService detailsService) {
		this.detailsService = detailsService;
	}
	
	@Override
	public void configure(HttpSecurity http) throws Exception {
		XAuthTokenFilter customFilter = new XAuthTokenFilter(detailsService);
		http.addFilterBefore(customFilter, UsernamePasswordAuthenticationFilter.class);
	}

}
