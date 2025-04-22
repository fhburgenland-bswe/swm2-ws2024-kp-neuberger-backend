package at.fhburgenland.bookmanager.integration;

import at.fhburgenland.bookmanager.dto.ReviewRequest;
import at.fhburgenland.bookmanager.model.Book;
import at.fhburgenland.bookmanager.model.Review;
import at.fhburgenland.bookmanager.model.User;
import at.fhburgenland.bookmanager.repository.BookRepository;
import at.fhburgenland.bookmanager.repository.ReviewRepository;
import at.fhburgenland.bookmanager.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ReviewIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private ReviewRepository reviewRepository;
    private User testUser;
    private Book testBook;

    @BeforeEach
    void setUp() {
        reviewRepository.deleteAll();
        bookRepository.deleteAll();
        userRepository.deleteAll();
        testUser = userRepository.save(User.builder()
                .name("Rezensionierer")
                .email("review@buch.at")
                .build());
        testBook = Book.builder()
                .isbn("9780140328721")
                .title("Matilda")
                .user(testUser)
                .build();
        testBook = bookRepository.save(testBook);
    }

    @Test
    void addReviewToBook_ReturnsCreated() throws Exception {
        ReviewRequest request = new ReviewRequest(5, "Sehr gut!");
        mockMvc.perform(post("/users/" + testUser.getId() + "/books/" + testBook.getIsbn() + "/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.reviewText").value("Sehr gut!"));
    }

    @Test
    void getAllReviews_ReturnsEmptyInitially() throws Exception {
        mockMvc.perform(get("/users/" + testUser.getId() + "/books/" + testBook.getIsbn() + "/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void updateReview_ReturnsUpdatedReview() throws Exception {
        Review review = reviewRepository.save(Review.builder()
                .rating(3)
                .reviewText("Geht so")
                .book(testBook)
                .build());
        ReviewRequest update = new ReviewRequest(4, "Besser als gedacht");
        mockMvc.perform(put("/users/" + testUser.getId() + "/books/" + testBook.getIsbn() + "/reviews/" + review.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(4))
                .andExpect(jsonPath("$.reviewText").value("Besser als gedacht"));
    }

    @Test
    void deleteReview_RemovesSuccessfully() throws Exception {
        Review saved = reviewRepository.save(Review.builder()
                .rating(2)
                .reviewText("Nicht meins")
                .book(testBook)
                .build());
        mockMvc.perform(delete("/users/" + testUser.getId() + "/books/" + testBook.getIsbn() + "/reviews/" + saved.getId()))
                .andExpect(status().isNoContent());
        assertThat(reviewRepository.findAll()).isEmpty();
    }

    @Test
    void addReview_InvalidRating_ReturnsValidationError() throws Exception {
        ReviewRequest invalid = new ReviewRequest(6, "Zu gut");
        mockMvc.perform(post("/users/" + testUser.getId() + "/books/" + testBook.getIsbn() + "/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", containsString("höchstens 5")));
    }
}
