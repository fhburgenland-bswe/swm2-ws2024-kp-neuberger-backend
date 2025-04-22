package at.fhburgenland.bookmanager.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO für das Erstellen oder Aktualisieren einer Rezension.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequest {

    @NotNull(message = "Bewertung darf nicht leer sein")
    @Min(value = 1, message = "Bewertung muss mindestens 1 sein")
    @Max(value = 5, message = "Bewertung darf höchstens 5 sein")
    private Integer rating;

    @NotBlank(message = "Rezensionstext darf nicht leer sein")
    private String reviewText;
}
