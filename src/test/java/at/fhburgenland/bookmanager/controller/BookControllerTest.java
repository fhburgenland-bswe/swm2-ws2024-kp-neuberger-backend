package at.fhburgenland.bookmanager.controller;

import at.fhburgenland.bookmanager.dto.IsbnRequest;
import at.fhburgenland.bookmanager.model.Book;
import at.fhburgenland.bookmanager.model.User;
import at.fhburgenland.bookmanager.repository.BookRepository;
import at.fhburgenland.bookmanager.repository.UserRepository;
import at.fhburgenland.bookmanager.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private BookService bookService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    private User testUser;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();
        testUser = userRepository.save(User.builder().name("Test User").email("testuser@books.at").build());
    }

    @Test
    void addBookByIsbn_ReturnsCreated() throws Exception {
        IsbnRequest isbnRequest = new IsbnRequest("1234567890");

        Book mockBook = Book.builder()
                .id(UUID.randomUUID())
                .isbn("1234567890")
                .title("Test Book")
                .user(testUser)
                .build();

        Mockito.when(bookService.addBookToUserByIsbn(eq(testUser.getId()), eq("1234567890")))
                .thenReturn(mockBook);

        mockMvc.perform(post("/users/{userId}/books", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(isbnRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.isbn").value("1234567890"))
                .andExpect(jsonPath("$.title").isString());
    }

    @Test
    void getBookByIsbn_ReturnsBookDetails() throws Exception {
        String isbn = "9780140328721";

        Book book = Book.builder()
                .isbn(isbn)
                .title("Matilda")
                .publisher("Puffin")
                .publishedDate("1988")
                .description("A story about a gifted girl")
                .coverUrl("https://covers.openlibrary.org/b/isbn/" + isbn + "-L.jpg")
                .user(testUser)
                .build();
        testUser.getBooks().add(book);
        userRepository.save(testUser);
        mockMvc.perform(get("/users/{userId}/books/{isbn}", testUser.getId(), isbn))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isbn").value(isbn))
                .andExpect(jsonPath("$.title").value("Matilda"))
                .andExpect(jsonPath("$.coverUrl", containsString(isbn)));
    }

    @Test
    void updateBookRating_ValidRequest_ReturnsUpdatedBook() throws Exception {
        String isbn = "9780140328721";
        Book book = Book.builder()
                .isbn(isbn)
                .title("Matilda")
                .rating(4)
                .user(testUser)
                .build();
        Book existingBook = Book.builder()
                .isbn("9780140328721")
                .title("Matilda")
                .user(testUser)
                .build();
        testUser.getBooks().add(existingBook);
        userRepository.save(testUser);

        Mockito.when(bookService.updateBookRating(eq(testUser.getId()), eq(isbn), eq(4)))
                .thenReturn(book);

        String body = """
        {
          "rating": 4
        }
        """;

        mockMvc.perform(put("/users/{userId}/books/{isbn}", testUser.getId(), isbn)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isbn").value(isbn))
                .andExpect(jsonPath("$.rating").value(4));
    }

    @Test
    void deleteBook_ExistingBook_ReturnsNoContent() throws Exception {
        String isbn = "9780140328721";
        Book book = Book.builder()
                .isbn(isbn)
                .title("Matilda")
                .user(testUser)
                .build();
        testUser.getBooks().add(book);
        userRepository.save(testUser);

        mockMvc.perform(delete("/users/" + testUser.getId() + "/books/" + isbn))
                .andExpect(status().isNoContent());

        assertThat(bookRepository.findAll()).isEmpty();
    }

    @Test
    void deleteBook_BookNotFound_Returns404() throws Exception {
        mockMvc.perform(delete("/users/" + testUser.getId() + "/books/0000000000"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Buch nicht gefunden"));
    }

    @Test
    void deleteBook_UserNotFound_Returns404() throws Exception {
        UUID unknownUser = UUID.randomUUID();
        mockMvc.perform(delete("/users/" + unknownUser + "/books/9780140328721"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Benutzer nicht gefunden"));
    }

    @Test
    void getBooksByRating_FilterWorksCorrectly() throws Exception {
        Book book1 = Book.builder().isbn("111").title("Buch 1").rating(2).user(testUser).build();
        Book book2 = Book.builder().isbn("222").title("Buch 2").rating(5).user(testUser).build();
        testUser.setBooks(List.of(book1, book2));
        userRepository.save(testUser);

        mockMvc.perform(get("/users/{userId}/books?rating=2", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].rating").value(2));
    }

    @Test
    void searchBooks_WithQueryParams_ReturnsFilteredBooks() throws Exception {
        Book book = Book.builder()
                .isbn("1234567890")
                .title("Der Hobbit")
                .authors(List.of("J.R.R. Tolkien"))
                .publishedDate("1937")
                .user(testUser)
                .build();

        testUser.setBooks(List.of(book));
        userRepository.save(testUser); // das ist wichtig – persistiert das Buch via Cascade

        mockMvc.perform(get("/users/{userId}/books/search", testUser.getId())
                        .param("title", "Hobbit")
                        .param("author", "Tolkien")
                        .param("year", "1937"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].isbn").value("1234567890"))
                .andExpect(jsonPath("$[0].title").value("Der Hobbit"))
                .andExpect(jsonPath("$[0].authors[0]").value("J.R.R. Tolkien"))
                .andExpect(jsonPath("$[0].publishedDate").value("1937"));
    }

    @Test
    void searchBooks_InvalidUUID_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/users/not-a-uuid/books/search")
                        .param("title", "irgendwas"))
                .andExpect(status().isBadRequest());
    }

}
