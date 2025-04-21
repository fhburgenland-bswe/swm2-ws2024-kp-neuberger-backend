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
}
