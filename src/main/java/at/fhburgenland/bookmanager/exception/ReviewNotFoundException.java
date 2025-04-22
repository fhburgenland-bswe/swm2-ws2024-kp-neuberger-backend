package at.fhburgenland.bookmanager.exception;

import java.util.UUID;

/**
 * Wird geworfen, wenn eine Rezension nicht gefunden wurde.
 */
public class ReviewNotFoundException extends RuntimeException {
    public ReviewNotFoundException(UUID reviewId) {
        super("Rezension mit der ID " + reviewId + " wurde nicht gefunden.");
    }
}
