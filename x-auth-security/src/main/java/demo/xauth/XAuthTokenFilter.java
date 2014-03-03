package demo.xauth;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Sifts through all incoming requests and installs a Spring Security principal
 * if a header corresponding to a valid user is found.
 *
 * @author Philip W. Sorst (philip@sorst.net)
 * @author Josh Long (josh@joshlong.com)
 */
public class XAuthTokenFilter extends GenericFilterBean {

    private final UserDetailsService detailsService;
    private final TokenUtils tokenUtils = new TokenUtils();
    private String xAuthTokenHeaderName = "x-auth-token";

    public XAuthTokenFilter(UserDetailsService userDetailsService) {
        this.detailsService = userDetailsService;
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