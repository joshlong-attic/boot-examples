package example;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import static org.springframework.http.MediaType.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/rest/news", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
public class NewsEntryResource {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private NewsEntryRepository newsEntryDao;

	@RequestMapping(method = RequestMethod.GET)
	public List<NewsEntry> list() throws Exception {
		this.logger.info("list()");
		List<NewsEntry> allEntries = this.newsEntryDao.findAll();
		// return viewWriter.writeValueAsString(allEntries);
		return allEntries;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/{id}")
	public ResponseEntity<NewsEntry> read(@PathVariable Long id) {
		this.logger.info("read(id)");
		NewsEntry newsEntry = this.newsEntryDao.findOne(id);
		ResponseEntity<NewsEntry> rNew = new ResponseEntity<NewsEntry>(null == newsEntry ? HttpStatus.NOT_FOUND : HttpStatus.OK);
		return rNew; 
	}

	@RequestMapping(method = RequestMethod.POST)
	public NewsEntry create(NewsEntry newsEntry) {
		this.logger.info("create(): " + newsEntry);
		return this.newsEntryDao.save(newsEntry);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/{id}")
	public NewsEntry update(@PathVariable Long id, NewsEntry newsEntry) {
		this.logger.info("update(): " + newsEntry);
		return this.newsEntryDao.save(newsEntry);
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/{id}")
	public void delete(@PathVariable Long id) {
		this.logger.info("delete(id)");
		this.newsEntryDao.delete(id);
	}

	// todo use Spring Security expressions to run this check declaratively
	// maybe even create a meta-annotation so that the check is typesafe
	// i think i did something like this in the Spring Security OAuth section of
	// the the-spring-rest-stack
	private boolean isAdmin() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Object principal = authentication.getPrincipal();
		if (principal instanceof String && ((String) principal).equals("anonymousUser")) {
			return false;
		}
		UserDetails userDetails = (UserDetails) principal;
		for (GrantedAuthority authority : userDetails.getAuthorities()) {
			if (authority.toString().equals("admin")) {
				return true;
			}
		}
		return false;
	}

}