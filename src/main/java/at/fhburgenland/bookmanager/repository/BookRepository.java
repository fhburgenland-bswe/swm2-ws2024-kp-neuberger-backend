package at.fhburgenland.bookmanager.repository;

import at.fhburgenland.bookmanager.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository zur Verwaltung von Büchern in der Datenbank.
 */
public interface BookRepository extends JpaRepository<Book, String> {
}
