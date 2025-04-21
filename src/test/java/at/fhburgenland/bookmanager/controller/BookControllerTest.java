package at.fhburgenland.bookmanager.controller;

import at.fhburgenland.bookmanager.dto.IsbnRequest;
import at.fhburgenland.bookmanager.model.Book;
import at.fhburgenland.bookmanager.model.User;
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
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
}
