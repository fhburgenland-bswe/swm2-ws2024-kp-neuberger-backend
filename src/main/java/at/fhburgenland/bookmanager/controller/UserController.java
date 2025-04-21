package at.fhburgenland.bookmanager.controller;

import at.fhburgenland.bookmanager.dto.UserDto;
import at.fhburgenland.bookmanager.model.User;
import at.fhburgenland.bookmanager.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST-Controller für Benutzeroperationen.
 */
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * Erstellt einen neuen Benutzer und gibt ihn zurück.
     *
     * @param userDto Eingabedaten für den Benutzer
     * @return Antwort mit Status 201 und erstelltem Benutzer
     */
    @PostMapping
    public ResponseEntity<User> createUser(@Valid @RequestBody UserDto userDto) {
        User createdUser = userService.createUser(userDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    /**
     * Gibt eine Liste aller registrierten Benutzer zurück.
     *
     * @return Liste der Benutzer mit HTTP 200 OK
     */
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    /**
     * Gibt die Details eines Benutzers anhand seiner ID zurück.
     *
     * @param userId Die ID des Benutzers
     * @return Antwort mit Status 200 und Benutzerdetails oder 404, falls nicht gefunden
     */
    @GetMapping("/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable UUID userId) {
        User user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    /**
     * Aktualisiert die Profildaten eines Benutzers.
     *
     * @param userId ID des Benutzers
     * @param userDto Neue Profildaten
     * @return Aktualisierter Benutzer
     */
    @PutMapping("/{userId}")
    public ResponseEntity<User> updateUser(@PathVariable UUID userId, @Valid @RequestBody UserDto userDto) {
        User updatedUser = userService.updateUser(userId, userDto);
        return ResponseEntity.ok(updatedUser);
    }
}
