package at.fhburgenland.bookmanager.service;

import at.fhburgenland.bookmanager.dto.ReviewRequest;
import at.fhburgenland.bookmanager.exception.BookNotFoundException;
import at.fhburgenland.bookmanager.exception.ReviewNotFoundException;
import at.fhburgenland.bookmanager.exception.UserNotFoundException;
import at.fhburgenland.bookmanager.model.Book;
import at.fhburgenland.bookmanager.model.Review;
import at.fhburgenland.bookmanager.model.User;
import at.fhburgenland.bookmanager.repository.BookRepository;
import at.fhburgenland.bookmanager.repository.ReviewRepository;
import at.fhburgenland.bookmanager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReviewServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private ReviewService reviewService;

    private UUID userId;
    private User user;
    private Book book;
    private Review review;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userId = UUID.randomUUID();
        book = Book.builder().isbn("1234567890").reviews(new ArrayList<>()).build();
        user = User.builder().id(userId).books(List.of(book)).build();
        review = Review.builder().id(UUID.randomUUID()).rating(3).reviewText("Nice").book(book).build();
    }

    @Test
    void addReview_ValidInput_SavesReview() {
        ReviewRequest request = new ReviewRequest(4, "Top Buch!");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(reviewRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        Review result = reviewService.addReview(userId, book.getIsbn(), request);
        assertEquals(4, result.getRating());
        assertEquals("Top Buch!", result.getReviewText());
        assertEquals(book, result.getBook());
        verify(reviewRepository).save(result);
    }

    @Test
    void getAllReviews_ReturnsList() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(reviewRepository.findByBook(book)).thenReturn(List.of(review));
        List<Review> result = reviewService.getAllReviews(userId, book.getIsbn());
        assertThat(result).hasSize(1).contains(review);
    }

    @Test
    void updateReview_ValidRequest_UpdatesReview() {
        ReviewRequest update = new ReviewRequest(5, "Wahnsinn!");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(reviewRepository.findById(review.getId())).thenReturn(Optional.of(review));
        when(reviewRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        Review result = reviewService.updateReview(userId, book.getIsbn(), review.getId(), update);
        assertEquals(5, result.getRating());
        assertEquals("Wahnsinn!", result.getReviewText());
    }

    @Test
    void deleteReview_ExistingReview_DeletesSuccessfully() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(reviewRepository.findById(review.getId())).thenReturn(Optional.of(review));
        reviewService.deleteReview(userId, book.getIsbn(), review.getId());
        verify(reviewRepository).delete(review);
    }

    @Test
    void addReview_UserNotFound_ThrowsException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> reviewService.addReview(userId, "isbn", new ReviewRequest(5, "text")));
    }

    @Test
    void updateReview_ReviewNotFound_ThrowsException() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(reviewRepository.findById(any())).thenReturn(Optional.empty());
        assertThrows(ReviewNotFoundException.class, () ->
                reviewService.updateReview(userId, book.getIsbn(), UUID.randomUUID(), new ReviewRequest(1, "neu")));
    }

    @Test
    void getBookForUser_BookNotFound_ThrowsException() {
        user.setBooks(List.of()); // kein Buch
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        assertThrows(BookNotFoundException.class, () -> reviewService.getAllReviews(userId, "fakeisbn"));
    }
}
