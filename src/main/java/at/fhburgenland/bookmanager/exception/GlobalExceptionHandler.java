package at.fhburgenland.bookmanager.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

/**
 * Zentrale Ausnahmebehandlung für REST-Controller.
 * Fängt Validierungsfehler (MethodArgumentNotValidException) ab
 * und gibt ein standardisiertes ProblemDetail-Objekt mit HTTP 400 zurück.
 *
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * Behandelt Validierungsfehler, die durch ungültige Request-Parameter entstehen.
     *
     * Liest alle Fehlermeldungen aus dem BindingResult aus,
     * verbindet sie zu einem einzigen String und verpackt
     * diese Informationen in ein {@link ProblemDetail}-Objekt.
     *
     *
     * @param ex die ausgelöste {@link MethodArgumentNotValidException}
     * @return ein {@link ProblemDetail} mit Status 400 (Bad Request),
     *         Titel "Validation failed" und Detail-Nachricht aller Fehler
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ProblemDetail handleValidationException(MethodArgumentNotValidException ex) {
        ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        detail.setTitle("Validation failed");
        detail.setDetail(
                ex.getBindingResult().getAllErrors().stream()
                        .map(err -> err.getDefaultMessage())
                        .collect(Collectors.joining("; "))
        );
        return detail;
    }

    /**
     * Globale Ausnahmebehandlung für unerwartete Fehler.
     *
     * Diese Methode fängt alle nicht explizit behandelten Exceptions ab,
     * um eine strukturierte Antwort mit HTTP-Status 500 zurückzugeben.
     * Dadurch wird vermieden, dass interne Fehler dem Client als
     * unstrukturierter Stacktrace angezeigt werden.
     *
     * @param ex Die aufgetretene Exception
     * @return Ein ProblemDetail-Objekt mit Fehlerbeschreibung
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ProblemDetail handleGenericException(Exception ex) {
        ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        detail.setTitle("Interner Fehler");
        detail.setDetail("Ein unerwarteter Fehler ist aufgetreten.");
        return detail;
    }

    /**
     * Behandelt den Fall, dass ein Benutzer anhand seiner ID nicht gefunden wird.
     *
     * Diese Methode fängt die ausgelöste UserNotFoundException ab
     * und erzeugt ein standardisiertes ProblemDetail-Objekt mit HTTP-Status 404 (Not Found).
     * Der Titel wird auf "Benutzer nicht gefunden" gesetzt, und das Detail enthält
     * die genaue Fehlermeldung aus der Exception.
     *
     * @param ex Die ausgelöste UserNotFoundException, die angibt, welche ID nicht gefunden wurde
     * @return Ein ProblemDetail-Objekt mit Status 404, Titel "Benutzer nicht gefunden"
     *         und Detail-Nachricht aus der Exception
     */
    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ProblemDetail handleUserNotFound(UserNotFoundException ex) {
        ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        detail.setTitle("Benutzer nicht gefunden");
        detail.setDetail(ex.getMessage());
        return detail;
    }
}
