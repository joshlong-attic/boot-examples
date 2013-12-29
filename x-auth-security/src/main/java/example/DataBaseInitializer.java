package example;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;


/**
 * Initialize the database with some test entries.
 * 
 * @author Philip W. Sorst <philip@sorst.net>
 */
@Component
public class DataBaseInitializer {

    private NewsEntryRepository newsEntryRepository ;
    private UserRepository userRepository;
	private PasswordEncoder passwordEncoder;

    @Autowired
	public DataBaseInitializer(UserRepository userDao, NewsEntryRepository newsEntryRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userDao;
		this.newsEntryRepository = newsEntryRepository;
		this.passwordEncoder = passwordEncoder;
	} 
    
	public void init () {

		User userUser = new User("user", this.passwordEncoder.encode("user"));
		userUser.addRole("user");
		userRepository.save(userUser);

		User adminUser = new User("admin", this.passwordEncoder.encode("admin"));
		adminUser.addRole("user");
		adminUser.addRole("admin");
        userRepository.save(adminUser);

		long timestamp = System.currentTimeMillis()  -  ( 1000 * 60 * 60 * 24);
		for (int i = 0; i < 10; i++) {
			NewsEntry newsEntry = new NewsEntry();
			newsEntry.setContent("This is example content " + i);
			newsEntry.setDate(new Date(timestamp));
			newsEntryRepository.save(newsEntry);
			timestamp += 1000 * 60 * 60;
		}
	}

}
