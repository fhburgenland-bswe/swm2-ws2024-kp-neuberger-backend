package at.fhburgenland.bookmanager.service;

import at.fhburgenland.bookmanager.dto.UserDto;
import at.fhburgenland.bookmanager.exception.UserAlreadyExistsException;
import at.fhburgenland.bookmanager.exception.UserNotFoundException;
import at.fhburgenland.bookmanager.model.User;
import at.fhburgenland.bookmanager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit-Test für den UserService.
 */
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private UserDto validUserDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        validUserDto = new UserDto("Max Mustermann", "max@beispiel.de");
    }

    @Test
    void createUser_Erfolgreich() {
        when(userRepository.findByEmail(validUserDto.getEmail())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.createUser(validUserDto);

        assertEquals(validUserDto.getName(), result.getName());
        assertEquals(validUserDto.getEmail(), result.getEmail());
    }

    @Test
    void createUser_EmailBereitsVorhanden_LöstExceptionAus() {
        when(userRepository.findByEmail(validUserDto.getEmail())).thenReturn(Optional.of(new User()));

        assertThrows(UserAlreadyExistsException.class, () -> userService.createUser(validUserDto));
    }

    @Test
    void getAllUsers_GibtListeZurück() {
        List<User> mockUsers = List.of(
                User.builder().name("User1").email("user1@example.com").build(),
                User.builder().name("User2").email("user2@example.com").build()
        );
        when(userRepository.findAll()).thenReturn(mockUsers);

        List<User> result = userService.getAllUsers();

        assertEquals(2, result.size());
        assertEquals("user1@example.com", result.get(0).getEmail());
    }

    @Test
    void getUserById_Vorhanden_ReturnsUser() {
        UUID id = UUID.randomUUID();
        User mock = new User(id, "Service Test", "service@test.at");
        when(userRepository.findById(id)).thenReturn(Optional.of(mock));

        User result = userService.getUserById(id);

        assertEquals("Service Test", result.getName());
        assertEquals("service@test.at", result.getEmail());
    }

    @Test
    void getUserById_NichtGefunden_WirftException() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUserById(id));
    }

    @Test
    void updateUser_Vorhanden_AktualisiertDaten() {
        UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        User existing = User.builder().id(userId).name("Alt").email("alt@mail.at").build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existing));
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        UserDto update = new UserDto("Neu", "neu@mail.at");
        User updated = userService.updateUser(userId, update);

        assertEquals("Neu", updated.getName());
        assertEquals("neu@mail.at", updated.getEmail());
    }

    @Test
    void updateUser_NichtVorhanden_LöstExceptionAus() {
        UUID missingId = UUID.fromString("99999999-9999-9999-9999-999999999999");
        when(userRepository.findById(missingId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> userService.updateUser(missingId, new UserDto("Test", "test@mail.at")));
    }
}

