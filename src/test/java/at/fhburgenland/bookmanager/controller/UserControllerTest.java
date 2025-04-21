package at.fhburgenland.bookmanager.controller;

import at.fhburgenland.bookmanager.dto.UserDto;
import at.fhburgenland.bookmanager.model.User;
import at.fhburgenland.bookmanager.repository.UserRepository;
import at.fhburgenland.bookmanager.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integrationstest für den UserController.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void cleanDb() {
        userRepository.deleteAll();
    }
    @Test
    void createUser_ValidInput_ReturnsCreated() throws Exception {
        UserDto userDto = new UserDto("Max Mustermann", "max@beispiel.de");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("max@beispiel.de"));
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
        userService.createUser(new UserDto("Lisa Musterfrau", "lisa@beispiel.at"));
        userService.createUser(new UserDto("Tom Tester", "tom@beispiel.at"));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].email").value("lisa@beispiel.at"))
                .andExpect(jsonPath("$[1].email").value("tom@beispiel.at"));
    }

    @Test
    void getUserById_Vorhanden_ReturnsOk() throws Exception {
        UserDto dto = new UserDto("Controller User", "controller@beispiel.at");
        User saved = userService.createUser(dto);

        mockMvc.perform(get("/users/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("controller@beispiel.at"));
    }

    @Test
    void getUserById_NichtGefunden_ReturnsNotFound() throws Exception {
        UserDto dto = new UserDto("Controller User", "controller@beispiel.at");
        User saved = userService.createUser(dto);
        UUID notFoundId = UUID.randomUUID();
        while (notFoundId.equals(saved.getId())) {
            notFoundId = UUID.randomUUID();
        }
        mockMvc.perform(get("/users/{id}", notFoundId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Benutzer nicht gefunden"));
    }

    @Test
    void updateUser_ValidInput_ReturnsUpdatedUser() throws Exception {
        UserDto update = new UserDto("Lisa Neu", "lisa.neu@beispiel.at");

        User user = userService.createUser(new UserDto("Lisa Alt", "lisa@beispiel.at"));

        mockMvc.perform(put("/users/" + user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Lisa Neu"))
                .andExpect(jsonPath("$.email").value("lisa.neu@beispiel.at"));
    }

    @Test
    void updateUser_InvalidInput_ReturnsBadRequest() throws Exception {
        UUID dummyId = UUID.randomUUID();
        UserDto invalid = new UserDto("", "keineemail");

        mockMvc.perform(put("/users/" + dummyId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteUser_Vorhanden_ReturnsNoContent() throws Exception {
        User user = userService.createUser(new UserDto("Delete Me", "delete@controller.at"));

        mockMvc.perform(delete("/users/" + user.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteUser_NichtVorhanden_ReturnsNotFound() throws Exception {
        UUID fakeId = UUID.randomUUID();

        mockMvc.perform(delete("/users/" + fakeId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Benutzer nicht gefunden"));
    }
}
