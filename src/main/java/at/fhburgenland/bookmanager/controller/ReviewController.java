package at.fhburgenland.bookmanager.controller;

import at.fhburgenland.bookmanager.dto.ReviewRequest;
import at.fhburgenland.bookmanager.model.Review;
import at.fhburgenland.bookmanager.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST-Controller für das Verwalten von Buchrezensionen.
 */
@RestController
@RequestMapping("/users/{userId}/books/{isbn}/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /**
     * Erstellt eine neue Rezension für ein Buch eines Benutzers.
     *
     * @param userId  ID des Benutzers
     * @param isbn    ISBN des Buches
     * @param request DTO mit Bewertung und Rezensionstext
     * @return HTTP 201 mit dem erstellten Review-Objekt
     */
    @PostMapping
    public ResponseEntity<Review> addReview(
            @PathVariable UUID userId,
            @PathVariable String isbn,
            @Valid @RequestBody ReviewRequest request
    ) {
        Review created = reviewService.addReview(userId, isbn, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Gibt alle Rezensionen eines Buches zurück.
     *
     * @param userId ID des Benutzers
     * @param isbn   ISBN des Buches
     * @return Liste aller Rezensionen (HTTP 200)
     */
    @GetMapping
    public ResponseEntity<List<Review>> getAllReviews(
            @PathVariable UUID userId,
            @PathVariable String isbn
    ) {
        return ResponseEntity.ok(reviewService.getAllReviews(userId, isbn));
    }

    /**
     * Aktualisiert eine vorhandene Rezension.
     *
     * @param userId   ID des Benutzers
     * @param isbn     ISBN des Buches
     * @param reviewId ID der zu aktualisierenden Rezension
     * @param request  DTO mit neuen Werten
     * @return HTTP 200 mit aktualisiertem Review
     */
    @PutMapping("/{reviewId}")
    public ResponseEntity<Review> updateReview(
            @PathVariable UUID userId,
            @PathVariable String isbn,
            @PathVariable UUID reviewId,
            @Valid @RequestBody ReviewRequest request
    ) {
        Review updated = reviewService.updateReview(userId, isbn, reviewId, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * Löscht eine Rezension.
     *
     * @param userId   ID des Benutzers
     * @param isbn     ISBN des Buches
     * @param reviewId ID der Rezension
     * @return HTTP 204 bei Erfolg
     */
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable UUID userId,
            @PathVariable String isbn,
            @PathVariable UUID reviewId
    ) {
        reviewService.deleteReview(userId, isbn, reviewId);
        return ResponseEntity.noContent().build();
    }
}
