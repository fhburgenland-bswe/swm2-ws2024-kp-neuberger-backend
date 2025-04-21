package at.fhburgenland.bookmanager.integration;

import at.fhburgenland.bookmanager.dto.IsbnRequest;
import at.fhburgenland.bookmanager.model.Book;
import at.fhburgenland.bookmanager.model.User;
import at.fhburgenland.bookmanager.repository.BookRepository;
import at.fhburgenland.bookmanager.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
class BookIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        bookRepository.deleteAll();
        userRepository.deleteAll();
        testUser = userRepository.save(User.builder()
                .name("Integration User")
                .email("integration@book.at")
                .books(new ArrayList<>())
                .build());
    }

    @Test
    void addBookToUser_ValidIsbn_ReturnsCreated() throws Exception {
        IsbnRequest request = new IsbnRequest("9780140328721"); // eine bekannte ISBN

        mockMvc.perform(post("/users/" + testUser.getId() + "/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.isbn").value("9780140328721"))
                .andExpect(jsonPath("$.title", not(emptyOrNullString())))
                .andExpect(jsonPath("$.coverUrl", containsString("9780140328721")));
    }

    @Test
    void addBookToUser_InvalidIsbn_ReturnsBadRequest() throws Exception {
        IsbnRequest request = new IsbnRequest("notarealisbn1234567890123");

        mockMvc.perform(post("/users/" + testUser.getId() + "/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Ungültige ISBN"))
                .andExpect(jsonPath("$.detail", containsString("Keine Daten gefunden")));
    }


    @Test
    void addBookToNonexistentUser_ReturnsNotFound() throws Exception {
        IsbnRequest request = new IsbnRequest("9780140328721");

        UUID unknownId = UUID.randomUUID();
        mockMvc.perform(post("/users/" + unknownId + "/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Benutzer nicht gefunden"));
    }

    @Test
    void getBookByIsbn_ExistingBook_ReturnsBookDetails() throws Exception {
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

        testUser.setBooks(new ArrayList<>(List.of(book)));
        userRepository.save(testUser);

        mockMvc.perform(get("/users/" + testUser.getId() + "/books/" + isbn))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isbn").value(isbn))
                .andExpect(jsonPath("$.title").value("Matilda"))
                .andExpect(jsonPath("$.publisher").value("Puffin"))
                .andExpect(jsonPath("$.coverUrl", containsString(isbn)));
    }

    @Test
    void getBookByIsbn_BookDoesNotExist_Returns404() throws Exception {
        String unknownIsbn = "0000000000";

        mockMvc.perform(get("/users/" + testUser.getId() + "/books/" + unknownIsbn))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Buch nicht gefunden"))
                .andExpect(jsonPath("$.detail", containsString(unknownIsbn)));
    }

    @Test
    void updateBookRating_ValidRequest_ReturnsUpdatedBook() throws Exception {
        String isbn = "9780140328721";
        Book book = Book.builder()
                .isbn(isbn)
                .title("Matilda")
                .rating(3)
                .user(testUser)
                .build();
        testUser.getBooks().add(book);
        userRepository.save(testUser);
        String json = """
        {
          "rating": 5
        }
        """;
        mockMvc.perform(put("/users/" + testUser.getId() + "/books/" + isbn)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isbn").value(isbn))
                .andExpect(jsonPath("$.rating").value(5));
    }

    @Test
    void updateBookRating_InvalidRating_ReturnsBadRequest() throws Exception {
        String isbn = "9780140328721";

        Book book = Book.builder()
                .isbn(isbn)
                .title("Matilda")
                .rating(3)
                .user(testUser)
                .build();

        testUser.getBooks().add(book);
        userRepository.save(testUser);

        String invalidRatingJson = """
    {
      "rating": 6
    }
    """;

        mockMvc.perform(put("/users/" + testUser.getId() + "/books/" + isbn)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRatingJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.detail", containsString("höchstens 5")));
    }

    @Test
    void updateBookRating_BookNotFound_ReturnsNotFound() throws Exception {
        String unknownIsbn = "0000000000";

        String ratingJson = """
    {
      "rating": 4
    }
    """;

        mockMvc.perform(put("/users/" + testUser.getId() + "/books/" + unknownIsbn)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ratingJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Buch nicht gefunden"))
                .andExpect(jsonPath("$.detail", containsString(unknownIsbn)));
    }
    @Test
    void deleteBook_ExistingBook_ReturnsNoContent() throws Exception {
        String isbn = "9780140328721";
        Book book = Book.builder()
                .isbn(isbn)
                .title("Matilda")
                .user(testUser)
                .build();

        testUser.setBooks(new ArrayList<>(List.of(book)));
        userRepository.save(testUser);

        mockMvc.perform(delete("/users/" + testUser.getId() + "/books/" + isbn))
                .andExpect(status().isNoContent());

        assertThat(bookRepository.findAll()).isEmpty();
    }

    @Test
    void deleteBook_BookNotFound_Returns404() throws Exception {
        mockMvc.perform(delete("/users/" + testUser.getId() + "/books/9999999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Buch nicht gefunden"));
    }

    @Test
    void deleteBook_UserNotFound_Returns404() throws Exception {
        UUID unknownUserId = UUID.randomUUID();
        mockMvc.perform(delete("/users/" + unknownUserId + "/books/9780140328721"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Benutzer nicht gefunden"));
    }

    @Test
    void getBooks_FilterByRating_ReturnsOnlyMatchingBooks() throws Exception {
        Book book1 = Book.builder().isbn("111").title("Buch 1").rating(3).user(testUser).build();
        Book book2 = Book.builder().isbn("222").title("Buch 2").rating(5).user(testUser).build();

        testUser.setBooks(List.of(book1, book2));
        userRepository.save(testUser);

        mockMvc.perform(get("/users/" + testUser.getId() + "/books")
                        .param("rating", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].isbn").value("222"));
    }

    @Test
    void getBooks_InvalidRating_ReturnsBadRequest() throws Exception {
        userRepository.save(testUser);

        mockMvc.perform(get("/users/" + testUser.getId() + "/books")
                        .param("rating", "7"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Ungültige Bewertung"))
                .andExpect(jsonPath("$.detail").value(containsString("zwischen 1 und 5")));
    }

}
