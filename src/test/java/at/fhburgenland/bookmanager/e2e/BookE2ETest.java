package at.fhburgenland.bookmanager.e2e;

import at.fhburgenland.bookmanager.dto.IsbnRequest;
import at.fhburgenland.bookmanager.model.Book;
import at.fhburgenland.bookmanager.model.User;
import at.fhburgenland.bookmanager.repository.BookRepository;
import at.fhburgenland.bookmanager.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BookE2ETest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    private User testUser;

    @BeforeEach
    void cleanUp() {
        bookRepository.deleteAll();
        userRepository.deleteAll();
        testUser = userRepository.save(User.builder()
                .name("E2E Testuser")
                .email("e2e@test.at")
                .build());
    }

    @Test
    void addBook_ValidIsbn_ReturnsCreatedBook() throws Exception {
        IsbnRequest request = new IsbnRequest("9780140328721");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(request), headers);

        ResponseEntity<Book> response = restTemplate.postForEntity(
                getUrl("/users/" + testUser.getId() + "/books"), entity, Book.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getIsbn()).isEqualTo("9780140328721");

        Optional<Book> saved = bookRepository.findAll().stream().findFirst();
        assertThat(saved).isPresent();
        assertThat(saved.get().getUser().getId()).isEqualTo(testUser.getId());
    }

    @Test
    void addBook_InvalidIsbn_ReturnsBadRequest() throws Exception {
        IsbnRequest request = new IsbnRequest("notarealisbn1234567890123");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(request), headers);

        try {
            restTemplate.postForEntity(
                    getUrl("/users/" + testUser.getId() + "/books"), entity, String.class);
            fail("Expected HttpClientErrorException.BadRequest");
        } catch (HttpClientErrorException.BadRequest ex) {
            assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(ex.getResponseBodyAsString()).contains("Ungültige ISBN");
        }
    }

    @Test
    void addBook_UserNichtGefunden_Returns404() throws Exception {
        UUID unknownId = UUID.randomUUID();
        IsbnRequest request = new IsbnRequest("9780140328721");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(request), headers);

        try {
            restTemplate.postForEntity(
                    getUrl("/users/" + unknownId + "/books"), entity, String.class);
            fail("Expected 404 Not Found");
        } catch (HttpClientErrorException.NotFound ex) {
            assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(ex.getResponseBodyAsString()).contains("Benutzer nicht gefunden");
        }
    }

    private String getUrl(String path) {
        return "http://localhost:" + port + path;
    }
}
