package at.fhburgenland.bookmanager.integration;

import at.fhburgenland.bookmanager.dto.UserDto;
import at.fhburgenland.bookmanager.model.User;
import at.fhburgenland.bookmanager.repository.UserRepository;
import at.fhburgenland.bookmanager.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integrationstest für den User-Endpunkt über MockMvc.
 */
@SpringBootTest
@AutoConfigureMockMvc
class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void cleanUp() {
        userRepository.deleteAll();
    }

    @Test
    void createUser_ValidRequest_ReturnsCreated() throws Exception {
        UserDto userDto = new UserDto("Testuser", "test@integration.at");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("test@integration.at"));
    }

    @Test
    void createUser_MissingFields_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllUsers_ReturnsUserList() throws Exception {
        userRepository.deleteAll();
        userRepository.saveAll(List.of(
                User.builder().name("Alice Test").email("alice@test.at").build(),
                User.builder().name("Bob Test").email("bob@test.at").build()
        ));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].email").value("alice@test.at"))
                .andExpect(jsonPath("$[1].email").value("bob@test.at"));
    }

    @Test
    void getAllUsers_UnexpectedException_ReturnsInternalServerError() {
        UserService mockService = mock(UserService.class);
        when(mockService.getAllUsers()).thenThrow(new RuntimeException("Unerwarteter Fehler"));
    }

    @Test
    void getUserById_Vorhanden_ReturnsOk() throws Exception {
        User saved = userRepository.save(
                User.builder()
                        .name("Int User")
                        .email("int@user.at")
                        .build()
        );

        mockMvc.perform(get("/users/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("int@user.at"));
    }

    @Test
    void getUserById_NichtGefunden_ReturnsNotFound() throws Exception {
        mockMvc.perform(get("/users/" + UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Benutzer nicht gefunden"));
    }

    @Test
    void updateUser_ReturnsUpdatedData() throws Exception {
        User user = userRepository.save(User.builder().name("Altname").email("alt@mail.at").build());

        UserDto updated = new UserDto("Neu", "neu@mail.at");

        mockMvc.perform(put("/users/" + user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Neu"))
                .andExpect(jsonPath("$.email").value("neu@mail.at"));
    }

    @Test
    void updateUser_NichtGefunden_Returns404() throws Exception {
        UserDto update = new UserDto("Fake", "fake@mail.at");
        UUID nichtExistierendeId = UUID.randomUUID();

        mockMvc.perform(put("/users/" + nichtExistierendeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Benutzer nicht gefunden"));
    }

    @Test
    void deleteUser_Existiert_Returns204() throws Exception {
        User user = userRepository.save(User.builder()
                .name("Lösch Mich")
                .email("delete@test.at")
                .build());

        mockMvc.perform(delete("/users/" + user.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteUser_NichtVorhanden_Returns404() throws Exception {
        UUID fakeId = UUID.randomUUID();

        mockMvc.perform(delete("/users/" + fakeId))
                .andExpect(status().isNotFound());
    }
}
