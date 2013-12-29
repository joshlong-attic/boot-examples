package example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ExampleUserDetailsService implements UserDetailsService {

	private UserRepository userRepository;

	@Autowired
	public ExampleUserDetailsService(UserRepository repository) {
		this.userRepository = repository;
	}

	@Transactional(readOnly = true)
	@Override
	public UserDetails loadUserByUsername(String username)
			throws UsernameNotFoundException {

		User user = this.userRepository.findByName(username);

		if (null == user) {
			throw new UsernameNotFoundException("The user with name "
					+ username + " was not found");
		}
		return user;
	}

}
