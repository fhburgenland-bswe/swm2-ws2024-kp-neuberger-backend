package at.fhburgenland.bookmanager.controller;

import at.fhburgenland.bookmanager.dto.UserDto;
import at.fhburgenland.bookmanager.model.User;
import at.fhburgenland.bookmanager.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

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
}
