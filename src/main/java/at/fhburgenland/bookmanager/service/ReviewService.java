package at.fhburgenland.bookmanager.service;

import at.fhburgenland.bookmanager.dto.ReviewRequest;
import at.fhburgenland.bookmanager.exception.BookNotFoundException;
import at.fhburgenland.bookmanager.exception.ReviewNotFoundException;
import at.fhburgenland.bookmanager.exception.UserNotFoundException;
import at.fhburgenland.bookmanager.model.Book;
import at.fhburgenland.bookmanager.model.Review;
import at.fhburgenland.bookmanager.model.User;
import at.fhburgenland.bookmanager.repository.BookRepository;
import at.fhburgenland.bookmanager.repository.ReviewRepository;
import at.fhburgenland.bookmanager.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Serviceklasse für das Verwalten von Rezensionen zu Büchern.
 */
@Service
public class ReviewService {

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final ReviewRepository reviewRepository;

    public ReviewService(UserRepository userRepository,
                         BookRepository bookRepository,
                         ReviewRepository reviewRepository) {
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.reviewRepository = reviewRepository;
    }

    /**
     * Fügt eine neue Rezension zu einem bestimmten Buch eines Benutzers hinzu.
     *
     * @param userId  ID des Benutzers
     * @param isbn    ISBN des Buches
     * @param request Bewertungs- und Textdaten
     * @return das erstellte Review
     */
    public Review addReview(UUID userId, String isbn, ReviewRequest request) {
        Book book = getBookForUser(userId, isbn);
        Review review = Review.builder()
                .rating(request.getRating())
                .reviewText(request.getReviewText())
                .book(book)
                .build();
        return reviewRepository.save(review);
    }

    /**
     * Gibt alle Rezensionen eines bestimmten Buchs zurück.
     *
     * @param userId ID des Benutzers
     * @param isbn   ISBN des Buches
     * @return Liste von Rezensionen
     */
    public List<Review> getAllReviews(UUID userId, String isbn) {
        Book book = getBookForUser(userId, isbn);
        return reviewRepository.findByBook(book);
    }

    /**
     * Aktualisiert eine bestehende Rezension.
     *
     * @param userId   ID des Benutzers
     * @param isbn     ISBN des Buches
     * @param reviewId ID der Rezension
     * @param request  Neue Werte für Bewertung und Text
     * @return das aktualisierte Review
     */
    public Review updateReview(UUID userId, String isbn, UUID reviewId, ReviewRequest request) {
        Book book = getBookForUser(userId, isbn);
        Review review = reviewRepository.findById(reviewId)
                .filter(r -> r.getBook().equals(book))
                .orElseThrow(() -> new ReviewNotFoundException(reviewId));
        review.setRating(request.getRating());
        review.setReviewText(request.getReviewText());
        return reviewRepository.save(review);
    }

    /**
     * Löscht eine Rezension eines Buches.
     *
     * @param userId   ID des Benutzers
     * @param isbn     ISBN des Buches
     * @param reviewId ID der zu löschenden Rezension
     */
    public void deleteReview(UUID userId, String isbn, UUID reviewId) {
        Book book = getBookForUser(userId, isbn);
        Review review = reviewRepository.findById(reviewId)
                .filter(r -> r.getBook().equals(book))
                .orElseThrow(() -> new ReviewNotFoundException(reviewId));
        reviewRepository.delete(review);
    }

    /**
     * Hilfsmethode zur Validierung von Benutzer- und Buchbezug.
     *
     * @param userId ID des Benutzers
     * @param isbn   ISBN des Buches
     * @return das zugehörige Book-Objekt
     */
    private Book getBookForUser(UUID userId, String isbn) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        return user.getBooks().stream()
                .filter(b -> b.getIsbn().equals(isbn))
                .findFirst()
                .orElseThrow(() -> new BookNotFoundException(isbn));
    }
}
