package at.fhburgenland.bookmanager.service;

import at.fhburgenland.bookmanager.dto.UserDto;
import at.fhburgenland.bookmanager.exception.UserNotFoundException;
import at.fhburgenland.bookmanager.model.User;
import at.fhburgenland.bookmanager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import at.fhburgenland.bookmanager.exception.UserAlreadyExistsException;

import java.util.List;
import java.util.UUID;

/**
 * Service zur Handhabung der Benutzerlogik.
 */
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Erstellt einen neuen Benutzer mit eindeutiger UUID.
     *
     * @param userDto Die Benutzerdaten
     * @return Der gespeicherte Benutzer
     * @throws UserAlreadyExistsException wenn die E-Mail bereits existiert
     */
    public User createUser(UserDto userDto) {
        if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Ein Benutzer mit dieser E-Mail existiert bereits.");
        }

        User user = User.builder()
                .name(userDto.getName())
                .email(userDto.getEmail())
                .build();

        return userRepository.save(user);
    }

    /**
     * Gibt alle im System gespeicherten Benutzer zurück.
     *
     * @return Liste aller Benutzer
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Gibt einen Benutzer anhand seiner ID zurück oder wirft, wenn nicht gefunden.
     *
     * @param id Die UUID des Benutzers
     * @return der gefundene User
     * @throws UserNotFoundException wenn kein User existiert
     */
    public User getUserById(UUID id) {
        return userRepository
                .findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }
}