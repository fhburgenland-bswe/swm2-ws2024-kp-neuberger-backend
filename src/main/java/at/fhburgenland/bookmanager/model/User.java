package at.fhburgenland.bookmanager.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.UUID;

/**
 * Repräsentiert einen Benutzer mit eindeutiger UUID, Name und E-Mail-Adresse.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users", uniqueConstraints = {@UniqueConstraint(columnNames = {"email"})})
public class User {

    /**
     * Eindeutige Benutzer-ID im UUID-Format.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Name des Benutzers.
     */
    @NotBlank
    private String name;

    /**
     * Gültige und eindeutige E-Mail-Adresse des Benutzers.
     */
    @Email
    @NotBlank
    private String email;
}
