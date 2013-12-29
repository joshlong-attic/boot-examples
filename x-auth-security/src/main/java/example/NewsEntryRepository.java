package example;

import org.springframework.data.jpa.repository.JpaRepository;


public interface NewsEntryRepository
 extends JpaRepository<NewsEntry,Long> {
}
