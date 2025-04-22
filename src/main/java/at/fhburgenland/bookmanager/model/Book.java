package at.fhburgenland.bookmanager.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Repräsentiert ein Buch, das einem Benutzer zugeordnet ist.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "books")
public class Book {

    /**
     * Eindeutige ID des Buchs.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Internationale Standardbuchnummer.
     */
    @NotBlank
    private String isbn;

    private String title;

    @ElementCollection
    private java.util.List<String> authors;

    private String publisher;

    private String publishedDate;

    private String description;

    private String coverUrl;

    /**
     * Bewertungsfeld (z. B. 1–5 Sterne), optional.
     */
    private Integer rating;

    /**
     * Zugehöriger Benutzer.
     */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    private User user;

    /**
     * Liste aller Rezensionen zu diesem Buch.
     */
    @Builder.Default
    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<Review> reviews = new ArrayList<>();
}
