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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
                .books(List.of())
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
}
