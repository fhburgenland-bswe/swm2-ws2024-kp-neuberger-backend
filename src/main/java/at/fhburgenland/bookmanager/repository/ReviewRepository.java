package at.fhburgenland.bookmanager.repository;

import at.fhburgenland.bookmanager.model.Book;
import at.fhburgenland.bookmanager.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Repository zur Verwaltung von Rezensionen.
 */
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    /**
     * Liefert alle Rezensionen zu einem bestimmten Buch.
     *
     * @param book das Buch, zu dem Rezensionen gesucht werden
     * @return Liste der zugehörigen Rezensionen
     */
    List<Review> findByBook(Book book);
}