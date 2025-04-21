package at.fhburgenland.bookmanager.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Ausnahme, die geworfen wird, wenn ein Benutzer mit der angegebenen E-Mail-Adresse bereits existiert.
 *
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class UserAlreadyExistsException extends RuntimeException {
    /**
     * Erstellt eine neue {@code UserAlreadyExistsException} mit einer benutzerdefinierten Fehlermeldung.
     *
     * @param message Die detaillierte Fehlermeldung
     */
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
