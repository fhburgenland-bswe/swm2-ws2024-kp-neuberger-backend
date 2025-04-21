package at.fhburgenland.bookmanager.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

/**
 * Ausnahme, wenn ein Benutzer anhand seiner ID nicht gefunden wird.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class UserNotFoundException extends RuntimeException {

  /**
   * Erstellt eine neue UserNotFoundException mit Nachricht.
   *
   * @param id Fehlermeldung
   */
  public UserNotFoundException(UUID id) {
    super("Benutzer mit ID " + id + " nicht gefunden");
  }
}
