package at.fhburgenland.bookmanager.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Wird geworfen, wenn ein Buch mit der angegebenen ISBN nicht gefunden wurde.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class BookNotFoundException extends RuntimeException {
    public BookNotFoundException(String isbn) {
        super("Buch mit ISBN " + isbn + " wurde nicht gefunden.");
    }
}
