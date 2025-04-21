package at.fhburgenland.bookmanager.dto;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * DTO für die Erstellung eines neuen Benutzers.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {

    /**
     * Name des Benutzers (Pflichtfeld).
     */
    @NotBlank(message = "Name ist erforderlich")
    private String name;

    /**
     * Gültige E-Mail-Adresse (Pflichtfeld).
     */
    @Email(message = "E-Mail muss gültig sein")
    @NotBlank(message = "E-Mail ist erforderlich")
    private String email;
}
