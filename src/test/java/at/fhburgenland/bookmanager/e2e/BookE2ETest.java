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

import java.util.List;
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

    @Test
    void getBookByIsbn_ExistingBook_ReturnsBookDetails() {
        Book book = Book.builder()
                .isbn("9780140328721")
                .title("Matilda")
                .publisher("Puffin")
                .publishedDate("1988")
                .description("A story about a gifted girl")
                .coverUrl("https://covers.openlibrary.org/b/isbn/9780140328721-L.jpg")
                .user(testUser)
                .build();

        testUser.getBooks().add(book);
        userRepository.save(testUser);

        ResponseEntity<Book> response = restTemplate.getForEntity(
                getUrl("/users/" + testUser.getId() + "/books/9780140328721"), Book.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTitle()).isEqualTo("Matilda");
        assertThat(response.getBody().getIsbn()).isEqualTo("9780140328721");
    }

    @Test
    void getBookByIsbn_BookNotFound_Returns404() {
        String isbn = "9999999999";

        try {
            restTemplate.getForEntity(
                    getUrl("/users/" + testUser.getId() + "/books/" + isbn), String.class);
            fail("Expected HttpClientErrorException.NotFound");
        } catch (HttpClientErrorException.NotFound ex) {
            assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(ex.getResponseBodyAsString()).contains("Buch nicht gefunden");
            assertThat(ex.getResponseBodyAsString()).contains(isbn);
        }
    }

    @Test
    void updateBook_InvalidRating_ReturnsBadRequest() throws Exception {
        Book book = Book.builder()
                .isbn("9780140328721")
                .title("Matilda")
                .rating(3)
                .user(testUser)
                .build();
        testUser.getBooks().add(book);
        userRepository.save(testUser);

        String jsonBody = """
        {
          "rating": 6
        }
        """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

        try {
            restTemplate.exchange(
                    getUrl("/users/" + testUser.getId() + "/books/9780140328721"),
                    HttpMethod.PUT,
                    entity,
                    String.class
            );
            fail("Expected HttpClientErrorException.BadRequest");
        } catch (HttpClientErrorException.BadRequest ex) {
            assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(ex.getResponseBodyAsString()).contains("Bewertung darf höchstens 5 sein");
        }
    }

    @Test
    void updateBook_RatingTooLow_ReturnsBadRequest() throws Exception {
        Book book = Book.builder()
                .isbn("9780140328721")
                .title("Matilda")
                .rating(3)
                .user(testUser)
                .build();
        testUser.getBooks().add(book);
        userRepository.save(testUser);

        String jsonBody = """
        {
          "rating": 0
        }
        """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

        try {
            restTemplate.exchange(
                    getUrl("/users/" + testUser.getId() + "/books/9780140328721"),
                    HttpMethod.PUT,
                    entity,
                    String.class
            );
            fail("Expected HttpClientErrorException.BadRequest");
        } catch (HttpClientErrorException.BadRequest ex) {
            assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(ex.getResponseBodyAsString()).contains("Bewertung muss mindestens 1 sein");
        }
    }

    @Test
    void updateBook_ValidRating_ReturnsUpdatedBook() throws Exception {
        Book book = Book.builder()
                .isbn("9780140328721")
                .title("Matilda")
                .rating(3)
                .user(testUser)
                .build();
        testUser.getBooks().add(book);
        userRepository.save(testUser);

        String jsonBody = """
        {
          "rating": 4
        }
        """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

        ResponseEntity<Book> response = restTemplate.exchange(
                getUrl("/users/" + testUser.getId() + "/books/9780140328721"),
                HttpMethod.PUT,
                entity,
                Book.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getRating()).isEqualTo(4);
        assertThat(response.getBody().getIsbn()).isEqualTo("9780140328721");
    }

    @Test
    void deleteBook_ExistingBook_Returns204() {
        Book book = Book.builder()
                .isbn("9780140328721")
                .title("Matilda")
                .user(testUser)
                .build();
        testUser.getBooks().add(book);
        userRepository.save(testUser);

        ResponseEntity<Void> response = restTemplate.exchange(
                getUrl("/users/" + testUser.getId() + "/books/9780140328721"),
                HttpMethod.DELETE,
                null,
                Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(bookRepository.findAll()).isEmpty();
    }

    @Test
    void deleteBook_BookNotFound_Returns404() {
        try {
            restTemplate.delete(getUrl("/users/" + testUser.getId() + "/books/0000000000"));
            fail("Expected 404 Not Found");
        } catch (HttpClientErrorException.NotFound ex) {
            assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(ex.getResponseBodyAsString()).contains("Buch nicht gefunden");
        }
    }

    @Test
    void deleteBook_UserNotFound_Returns404() {
        UUID unknownUserId = UUID.randomUUID();

        try {
            restTemplate.delete(getUrl("/users/" + unknownUserId + "/books/9780140328721"));
            fail("Expected 404 Not Found");
        } catch (HttpClientErrorException.NotFound ex) {
            assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(ex.getResponseBodyAsString()).contains("Benutzer nicht gefunden");
        }
    }

    @Test
    void filterBooksByRating_Valid_ReturnsFilteredList() throws Exception {
        Book book1 = Book.builder().isbn("111").title("Test 1").rating(3).user(testUser).build();
        Book book2 = Book.builder().isbn("222").title("Test 2").rating(5).user(testUser).build();
        testUser.setBooks(List.of(book1, book2));
        userRepository.save(testUser);

        ResponseEntity<Book[]> response = restTemplate.getForEntity(
                getUrl("/users/" + testUser.getId() + "/books?rating=5"), Book[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody()[0].getRating()).isEqualTo(5);
    }

    @Test
    void filterBooksByRating_InvalidRating_ReturnsBadRequest() {
        try {
            restTemplate.getForEntity(
                    getUrl("/users/" + testUser.getId() + "/books?rating=8"), String.class);
            fail("Expected BadRequest");
        } catch (HttpClientErrorException.BadRequest ex) {
            assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(ex.getResponseBodyAsString()).contains("zwischen 1 und 5");
        }
    }

}
