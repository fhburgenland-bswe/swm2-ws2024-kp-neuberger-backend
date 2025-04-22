package at.fhburgenland.bookmanager.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.UUID;

/**
 * Repräsentiert eine einzelne Rezension zu einem Buch.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    /**
     * Eindeutige ID der Rezension.
     */
    @Id
    @GeneratedValue
    private UUID id;

    /**
     * Bewertung in numerischer Form (1–5).
     */
    @Min(value = 1, message = "Bewertung muss mindestens 1 sein")
    @Max(value = 5, message = "Bewertung darf höchstens 5 sein")
    private int rating;

    /**
     * Freitext der Rezension.
     */
    @NotBlank(message = "Rezensionstext darf nicht leer sein")
    private String reviewText;

    /**
     * Das Buch, zu dem diese Rezension gehört.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "book_id")
    @JsonBackReference
    private Book book;
}