package at.fhburgenland.bookmanager.controller;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ReviewControllerTest {

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
    void setup() {
        reviewRepository.deleteAll();
        bookRepository.deleteAll();
        userRepository.deleteAll();
        testUser = userRepository.save(User.builder()
                .name("Test Reviewer")
                .email("review@example.com")
                .build());
        testBook = bookRepository.save(Book.builder()
                .isbn("1234567890")
                .title("Testbuch")
                .user(testUser)
                .build());
    }

    @Test
    void addReview_ReturnsCreated() throws Exception {
        ReviewRequest request = new ReviewRequest(5, "Super!");
        mockMvc.perform(post("/users/{userId}/books/{isbn}/reviews", testUser.getId(), testBook.getIsbn())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.reviewText").value("Super!"));
    }

    @Test
    void getAllReviews_ReturnsList() throws Exception {
        Review review = Review.builder()
                .rating(4)
                .reviewText("Gut")
                .book(testBook)
                .build();
        reviewRepository.save(review);
        mockMvc.perform(get("/users/{userId}/books/{isbn}/reviews", testUser.getId(), testBook.getIsbn()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].rating").value(4))
                .andExpect(jsonPath("$[0].reviewText").value("Gut"));
    }

    @Test
    void updateReview_ValidReview_ReturnsUpdated() throws Exception {
        Review existing = reviewRepository.save(Review.builder()
                .rating(2)
                .reviewText("Okay")
                .book(testBook)
                .build());
        ReviewRequest update = new ReviewRequest(4, "Besser als gedacht");
        mockMvc.perform(put("/users/{userId}/books/{isbn}/reviews/{reviewId}",
                        testUser.getId(), testBook.getIsbn(), existing.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(4))
                .andExpect(jsonPath("$.reviewText").value("Besser als gedacht"));
    }

    @Test
    void deleteReview_ValidReview_ReturnsNoContent() throws Exception {
        Review review = reviewRepository.save(Review.builder()
                .rating(1)
                .reviewText("Nicht mein Ding")
                .book(testBook)
                .build());
        mockMvc.perform(delete("/users/{userId}/books/{isbn}/reviews/{reviewId}",
                        testUser.getId(), testBook.getIsbn(), review.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void addReview_InvalidRequest_ReturnsBadRequest() throws Exception {
        ReviewRequest invalid = new ReviewRequest(null, null);

        mockMvc.perform(post("/users/{userId}/books/{isbn}/reviews", testUser.getId(), testBook.getIsbn())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"));
    }
}
