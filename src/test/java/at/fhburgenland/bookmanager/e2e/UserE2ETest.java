package at.fhburgenland.bookmanager.e2e;

import at.fhburgenland.bookmanager.dto.UserDto;
import at.fhburgenland.bookmanager.model.User;
import at.fhburgenland.bookmanager.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;

import static org.aspectj.bridge.MessageUtil.fail;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-End-Test für Benutzer-Endpoint über echten HTTP-Aufruf.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserE2ETest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void cleanUp() {
        userRepository.deleteAll();
    }

    @Test
    void createUser_E2E_ReturnsCreatedAndPersists() throws Exception {
        UserDto userDto = new UserDto("Anna E2E", "anna@e2e.at");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(userDto), headers);

        ResponseEntity<User> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/users", entity, User.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getEmail()).isEqualTo("anna@e2e.at");
        assertThat(userRepository.findByEmail("anna@e2e.at")).isPresent();
    }

    @Test
    void getAllUsers_E2E_ReturnsUserList() {
        userRepository.saveAll(List.of(
                User.builder().name("Alice").email("alice@e2e.at").build(),
                User.builder().name("Bob").email("bob@e2e.at").build()
        ));

        ResponseEntity<User[]> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/users", User[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isGreaterThanOrEqualTo(2);
    }

    @Test
    void getUserById_E2E_ReturnsUser() {
        User saved = userRepository.save(
                User.builder()
                        .name("E2E User")
                        .email("e2euser@test.at")
                        .build()
        );

        ResponseEntity<User> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/users/" + saved.getId(),
                User.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getEmail()).isEqualTo("e2euser@test.at");
    }

    @Test
    void getUserById_E2E_NotFound() {
        UUID randomId = UUID.randomUUID();
        try {
            restTemplate.getForEntity(
                    "http://localhost:" + port + "/users/" + randomId,
                    String.class
            );
            fail("Expected HttpClientErrorException.NotFound");
        } catch (HttpClientErrorException.NotFound ex) {
            assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(ex.getResponseBodyAsString()).contains("Benutzer nicht gefunden");
        }
    }

    @Test
    void updateUser_E2E_ReturnsUpdatedUser() {
        User saved = userRepository.save(User.builder().name("E2E Alt").email("alt@e2e.at").build());

        UserDto updated = new UserDto("E2E Neu", "neu@e2e.at");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<UserDto> request = new HttpEntity<>(updated, headers);

        ResponseEntity<User> response = restTemplate.exchange(
                "http://localhost:" + port + "/users/" + saved.getId(),
                HttpMethod.PUT,
                request,
                User.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getEmail()).isEqualTo("neu@e2e.at");
    }

    @Test
    void updateUser_E2E_UserNichtGefunden_Returns404() throws Exception {
        UserDto updated = new UserDto("Niemand", "niemand@e2e.at");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<UserDto> request = new HttpEntity<>(updated, headers);

        UUID nonExistingId = UUID.randomUUID();

        try {
            restTemplate.exchange(
                    "http://localhost:" + port + "/users/" + nonExistingId,
                    HttpMethod.PUT,
                    request,
                    String.class
            );
            fail("Erwartete HttpClientErrorException wurde nicht geworfen.");
        } catch (HttpClientErrorException.NotFound ex) {
            assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(ex.getResponseBodyAsString()).contains("Benutzer mit ID");
        }
    }
}
