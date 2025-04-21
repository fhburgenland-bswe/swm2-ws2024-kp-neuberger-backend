package at.fhburgenland.bookmanager.service;

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
}
