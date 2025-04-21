package at.fhburgenland.bookmanager.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * DTO zur Aktualisierung der Bewertung eines Buches.
 * Akzeptiert nur Werte zwischen 1 und 5.
 */
public class RatingUpdateRequest {

    @NotNull(message = "Die Bewertung darf nicht leer sein")
    @Min(value = 1, message = "Bewertung muss mindestens 1 sein")
    @Max(value = 5, message = "Bewertung darf höchstens 5 sein")
    private Integer rating;

    public RatingUpdateRequest() {
    }

    public RatingUpdateRequest(Integer rating) {
        this.rating = rating;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }
}
