package example;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class UserResource {

    @Autowired
    private UserDetailsService userService;

    @Autowired
    private TokenUtils tokenUtils;

    @Autowired
    private AuthenticationManager authManager;

    @RequestMapping( produces = MediaType.APPLICATION_JSON_VALUE, 
    		         value = "/rest/user/authenticate", method = RequestMethod.POST)
    public UserTransfer authenticate(@RequestParam("username") String username,
                                     @RequestParam("password") String password) {

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);
        Authentication authentication = this.authManager.authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);        
        Map<String, Boolean> roles = new HashMap<String, Boolean>();

		/*
         * Reload user as password of authentication principal will be null after authorization and
		 * password is needed for token generation
		 */
        UserDetails userDetails = this.userService.loadUserByUsername(username);

        for (GrantedAuthority authority : userDetails.getAuthorities()) {
            roles.put(authority.toString(), Boolean.TRUE);
        }

        return new UserTransfer(userDetails.getUsername(), roles, tokenUtils.createToken(userDetails));
    }
}
