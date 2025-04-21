package at.fhburgenland.bookmanager.service;

import at.fhburgenland.bookmanager.exception.BookNotFoundException;
import at.fhburgenland.bookmanager.exception.InvalidBookException;
import at.fhburgenland.bookmanager.exception.UserNotFoundException;
import at.fhburgenland.bookmanager.model.Book;
import at.fhburgenland.bookmanager.model.User;
import at.fhburgenland.bookmanager.repository.BookRepository;
import at.fhburgenland.bookmanager.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

class BookServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private RestTemplate restTemplate;

    private ObjectMapper objectMapper;

    private BookService bookService;

    private UUID userId;
    private User mockUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
        userId = UUID.randomUUID();
        mockUser = User.builder()
                .id(userId)
                .name("Service Tester")
                .email("service@test.at")
                .books(new ArrayList<>())
                .build();
        bookService = new BookService(
                "https://openlibrary.org/api/books",
                userRepository,
                bookRepository,
                objectMapper
        );
        injectMockRestTemplate(bookService, restTemplate);
    }

    private void injectMockRestTemplate(BookService service, RestTemplate mock) {
        try {
            var field = BookService.class.getDeclaredField("restTemplate");
            field.setAccessible(true);
            field.set(service, mock);
        } catch (Exception e) {
            throw new RuntimeException("Fehler beim Injecten des RestTemplates", e);
        }
    }

    @Test
    void addBookToUserByIsbn_ValidResponse_ReturnsBook() throws Exception {
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        String isbn = "9780140328721";
        String jsonResponse = """
        {
          "ISBN:9780140328721": {
            "title": "Matilda",
            "publishers": ["Puffin"],
            "publish_date": "1988",
            "description": {"value": "A story about a gifted girl"}
          }
        }
        """;
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(new ResponseEntity<>(jsonResponse, HttpStatus.OK));
        Book created = bookService.addBookToUserByIsbn(userId, isbn);
        assertEquals("Matilda", created.getTitle());
        assertEquals("9780140328721", created.getIsbn());
        assertEquals("Puffin", created.getPublisher());
        assertEquals("1988", created.getPublishedDate());
        assertEquals("A story about a gifted girl", created.getDescription());
        verify(userRepository).save(mockUser);
        assertEquals(1, mockUser.getBooks().size());
    }

    @Test
    void addBookToUserByIsbn_UserNotFound_ThrowsException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class,
                () -> bookService.addBookToUserByIsbn(userId, "123"));
    }

    @Test
    void addBookToUserByIsbn_InvalidApiResponse_ThrowsException() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(new ResponseEntity<>("{}", HttpStatus.OK));
        assertThrows(InvalidBookException.class,
                () -> bookService.addBookToUserByIsbn(userId, "0000000000"));
    }

    @Test
    void addBookToUserByIsbn_BadHttpResponse_ThrowsException() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR));
        assertThrows(InvalidBookException.class,
                () -> bookService.addBookToUserByIsbn(userId, "123"));
    }

    @Test
    void getBookByUserIdAndIsbn_ExistingBook_ReturnsBook() {
        String isbn = "1234567890";
        Book book = Book.builder()
                .isbn(isbn)
                .title("Unit Test Book")
                .user(mockUser)
                .build();

        mockUser.getBooks().add(book);
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        Book result = bookService.getBookByUserIdAndIsbn(userId, isbn);

        assertNotNull(result);
        assertEquals(isbn, result.getIsbn());
        assertEquals("Unit Test Book", result.getTitle());
    }

    @Test
    void getBookByUserIdAndIsbn_BookNotFound_ThrowsException() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        assertThrows(BookNotFoundException.class,
                () -> bookService.getBookByUserIdAndIsbn(userId, "notfoundisbn"));
    }

    @Test
    void getBookByUserIdAndIsbn_UserNotFound_ThrowsException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> bookService.getBookByUserIdAndIsbn(userId, "1234567890"));
    }

    @Test
    void updateBookRating_ValidRating_UpdatesSuccessfully() {
        Book book = Book.builder()
                .isbn("9780140328721")
                .rating(2)
                .user(mockUser)
                .build();

        mockUser.getBooks().add(book);

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        Book updated = bookService.updateBookRating(userId, "9780140328721", 5);

        assertEquals(5, updated.getRating());
        verify(userRepository).save(mockUser);
    }

    @Test
    void updateBookRating_InvalidRating_ThrowsException() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        assertThrows(InvalidBookException.class, () -> {
            bookService.updateBookRating(userId, "9780140328721", 0);
        });
    }

    @Test
    void updateBookRating_BookNotFound_ThrowsException() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        assertThrows(BookNotFoundException.class, () -> {
            bookService.updateBookRating(userId, "notfound", 4);
        });
    }

    @Test
    void deleteBookByUserIdAndIsbn_BookExists_DeletesSuccessfully() {
        Book book = Book.builder()
                .isbn("1234567890")
                .user(mockUser)
                .build();

        mockUser.getBooks().add(book);
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        bookService.deleteBookByUserIdAndIsbn(userId, "1234567890");

        verify(bookRepository).delete(book);
        assertThat(mockUser.getBooks()).doesNotContain(book);
    }

    @Test
    void deleteBookByUserIdAndIsbn_BookNotFound_ThrowsException() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        assertThrows(BookNotFoundException.class, () -> bookService.deleteBookByUserIdAndIsbn(userId, "999"));
    }

    @Test
    void deleteBookByUserIdAndIsbn_UserNotFound_ThrowsException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> bookService.deleteBookByUserIdAndIsbn(userId, "1234567890"));
    }

    @Test
    void getBooksByUserIdAndOptionalRating_NoRating_ReturnsAllBooks() {
        Book book1 = Book.builder().isbn("111").title("Buch 1").rating(3).build();
        Book book2 = Book.builder().isbn("222").title("Buch 2").rating(5).build();
        mockUser.setBooks(List.of(book1, book2));
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        List<Book> result = bookService.getBooksByUserIdAndOptionalRating(userId, null);

        assertThat(result).hasSize(2);
    }

    @Test
    void getBooksByUserIdAndOptionalRating_ValidRating_ReturnsMatchingBooks() {
        Book book1 = Book.builder().isbn("111").title("Buch 1").rating(2).build();
        Book book2 = Book.builder().isbn("222").title("Buch 2").rating(5).build();
        mockUser.setBooks(List.of(book1, book2));
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        List<Book> result = bookService.getBooksByUserIdAndOptionalRating(userId, 2);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRating()).isEqualTo(2);
    }

    @Test
    void getBooksByUserIdAndOptionalRating_InvalidRating_ThrowsException() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        assertThrows(InvalidBookException.class,
                () -> bookService.getBooksByUserIdAndOptionalRating(userId, 10));
    }

    @Test
    void getBooksByUserIdAndOptionalRating_UserNotFound_ThrowsException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> bookService.getBooksByUserIdAndOptionalRating(userId, 3));
    }

}
