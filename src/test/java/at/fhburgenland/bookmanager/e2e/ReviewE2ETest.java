package at.fhburgenland.bookmanager.e2e;

import at.fhburgenland.bookmanager.dto.ReviewRequest;
import at.fhburgenland.bookmanager.model.Book;
import at.fhburgenland.bookmanager.model.Review;
import at.fhburgenland.bookmanager.model.User;
import at.fhburgenland.bookmanager.repository.BookRepository;
import at.fhburgenland.bookmanager.repository.ReviewRepository;
import at.fhburgenland.bookmanager.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ReviewE2ETest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private ReviewRepository reviewRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private User testUser;
    private Book testBook;

    @BeforeEach
    void setup() {
        reviewRepository.deleteAll();
        bookRepository.deleteAll();
        userRepository.deleteAll();
        testUser = userRepository.save(User.builder()
                .name("Review Tester")
                .email("review@test.at")
                .build());
        testBook = bookRepository.save(Book.builder()
                .isbn("9780140328721")
                .title("Matilda")
                .user(testUser)
                .build());
    }

    @Test
    void addReview_ValidRequest_ReturnsCreated() throws Exception {
        ReviewRequest request = new ReviewRequest(5, "Top!");
        HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(request), defaultHeaders());
        ResponseEntity<Review> response = restTemplate.postForEntity(
                getUrl("/users/" + testUser.getId() + "/books/" + testBook.getIsbn() + "/reviews"),
                entity,
                Review.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getRating()).isEqualTo(5);
        assertThat(response.getBody().getReviewText()).isEqualTo("Top!");
    }

    @Test
    void addReview_InvalidRating_Returns400() throws Exception {
        ReviewRequest request = new ReviewRequest(0, "Fail");
        HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(request), defaultHeaders());
        try {
            restTemplate.postForEntity(
                    getUrl("/users/" + testUser.getId() + "/books/" + testBook.getIsbn() + "/reviews"),
                    entity,
                    String.class);
            fail("Expected 400");
        } catch (HttpClientErrorException.BadRequest ex) {
            assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(ex.getResponseBodyAsString()).contains("mindestens 1");
        }
    }

    @Test
    void getAllReviews_InitiallyEmpty_Returns200() {
        ResponseEntity<Review[]> response = restTemplate.getForEntity(
                getUrl("/users/" + testUser.getId() + "/books/" + testBook.getIsbn() + "/reviews"),
                Review[].class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void updateReview_ValidRequest_ReturnsUpdatedReview() throws Exception {
        Review saved = reviewRepository.save(Review.builder()
                .rating(3)
                .reviewText("Ganz gut")
                .book(testBook)
                .build());
        ReviewRequest update = new ReviewRequest(4, "Noch besser");
        HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(update), defaultHeaders());
        ResponseEntity<Review> response = restTemplate.exchange(
                getUrl("/users/" + testUser.getId() + "/books/" + testBook.getIsbn() + "/reviews/" + saved.getId()),
                HttpMethod.PUT,
                entity,
                Review.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getRating()).isEqualTo(4);
        assertThat(response.getBody().getReviewText()).isEqualTo("Noch besser");
    }

    @Test
    void deleteReview_ExistingReview_Returns204() {
        Review saved = reviewRepository.save(Review.builder()
                .rating(2)
                .reviewText("Schwach")
                .book(testBook)
                .build());
        ResponseEntity<Void> response = restTemplate.exchange(
                getUrl("/users/" + testUser.getId() + "/books/" + testBook.getIsbn() + "/reviews/" + saved.getId()),
                HttpMethod.DELETE,
                null,
                Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(reviewRepository.findAll()).isEmpty();
    }

    @Test
    void deleteReview_NotFound_Returns404() {
        UUID fakeId = UUID.randomUUID();
        try {
            restTemplate.delete(getUrl("/users/" + testUser.getId() + "/books/" + testBook.getIsbn() + "/reviews/" + fakeId));
            fail("Expected 404");
        } catch (HttpClientErrorException.NotFound ex) {
            assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(ex.getResponseBodyAsString()).contains("Rezension");
        }
    }

    private String getUrl(String path) {
        return "http://localhost:" + port + path;
    }

    private HttpHeaders defaultHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
