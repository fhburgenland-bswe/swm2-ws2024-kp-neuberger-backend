package at.fhburgenland.bookmanager.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * InvalidBookException.java
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidBookException extends RuntimeException {
    public InvalidBookException(String message) {
        super(message);
    }
}
