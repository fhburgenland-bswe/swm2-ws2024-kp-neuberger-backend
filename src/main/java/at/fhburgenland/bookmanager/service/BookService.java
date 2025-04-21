package at.fhburgenland.bookmanager.service;

import at.fhburgenland.bookmanager.exception.BookNotFoundException;
import at.fhburgenland.bookmanager.exception.InvalidBookException;
import at.fhburgenland.bookmanager.exception.UserNotFoundException;
import at.fhburgenland.bookmanager.model.Book;
import at.fhburgenland.bookmanager.model.User;
import at.fhburgenland.bookmanager.repository.BookRepository;
import at.fhburgenland.bookmanager.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Service für die Buchverwaltung (synchron, ohne Reactive).
 * Fügt ein Buch per ISBN hinzu, holt die Details von der OpenLibrary API
 * und speichert es dem Benutzer zu.
 */
@Service
public class BookService {
    private final RestTemplate restTemplate;
    private final String apiUrl;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final BookRepository bookRepository;

    /**
     * Konstruktor für den BookService.
     *
     * Initialisiert den Service mit der OpenLibrary-API-URL sowie den benötigten Repositories und einem ObjectMapper.
     * Der Service nutzt einen synchronen {@link RestTemplate}, um Buchdetails basierend auf einer ISBN von der
     * OpenLibrary API abzurufen. Die Coverbilder werden als URL im Buch gespeichert.
     *
     * @param apiUrl         Die Basis-URL der OpenLibrary Books API (wird aus application.properties geladen).
     * @param userRepository Repository zur Verwaltung der Benutzer in der Datenbank.
     * @param bookRepository Repository zur Verwaltung der Bücher in der Datenbank.
     * @param objectMapper   Jackson-ObjectMapper zur Verarbeitung der JSON-Antworten von OpenLibrary.
     */
    public BookService(@Value("${book.api.url}") String apiUrl,
                       UserRepository userRepository,
                       BookRepository bookRepository,
                       ObjectMapper objectMapper) {
        this.restTemplate = new RestTemplate();
        this.apiUrl       = apiUrl;
        this.userRepository = userRepository;
        this.objectMapper   = objectMapper;
        this.bookRepository = bookRepository;

    }

    /**
     * Fügt ein Buch anhand seiner ISBN zum Benutzer hinzu.
     *
     * @param userId ID des Benutzers
     * @param isbn   ISBN als String
     * @return das neu angelegte und dem Nutzer zugeordnete Buch
     * @throws UserNotFoundException bei unbekannter userId
     * @throws InvalidBookException  bei Fehlern beim Abruf oder Parsen der Buchdaten
     */
    public Book addBookToUserByIsbn(UUID userId, String isbn) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        String url = String.format("%s?bibkeys=ISBN:%s&format=json&jscmd=data", apiUrl, isbn);
        ResponseEntity<String> resp = restTemplate.getForEntity(url, String.class);
        if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
            throw new InvalidBookException("Fehler beim Abruf der Buchdaten für ISBN: " + isbn);
        }

        try {
            JsonNode root = objectMapper.readTree(resp.getBody());
            JsonNode data = root.path("ISBN:" + isbn);
            if (data.isMissingNode() || data.isEmpty()) {
                throw new InvalidBookException("Keine Daten gefunden für ISBN: " + isbn);
            }

            Book book = Book.builder()
                    .isbn(isbn)
                    .title(data.path("title").asText(""))
                    .publisher(
                            data.path("publishers").isArray() && !data.path("publishers").isEmpty()
                                    ? data.path("publishers").get(0).asText("")
                                    : ""
                    )
                    .publishedDate(data.path("publish_date").asText(""))
                    .description(
                            data.path("description").has("value")
                                    ? data.path("description").path("value").asText("")
                                    : data.path("description").asText("")
                    )
                    .coverUrl("https://covers.openlibrary.org/b/isbn/" + isbn + "-L.jpg")
                    .build();

            book.setUser(user);
            user.getBooks().add(book);

            userRepository.save(user);

            return book;
        } catch (IOException e) {
            throw new InvalidBookException("Fehler beim Parsen der Buchdaten: " + e.getMessage());
        }
    }

    /**
     * Aktualisiert die Bewertung eines Buchs anhand der ISBN und Benutzer-ID.
     *
     * @param userId Die ID des Benutzers
     * @param isbn   Die ISBN des Buches
     * @param rating Neue Bewertung (zwischen 1 und 5)
     * @return Das aktualisierte Buchobjekt
     * @throws UserNotFoundException Wenn der Benutzer nicht existiert
     * @throws BookNotFoundException Wenn das Buch nicht gefunden wurde
     * @throws InvalidBookException  Wenn die Bewertung ungültig ist
     */
    public Book updateBookRating(UUID userId, String isbn, int rating) {
        if (rating < 1 || rating > 5) {
            throw new InvalidBookException("Die Bewertung muss zwischen 1 und 5 liegen.");
        }

        var user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        var book = user.getBooks().stream()
                .filter(b -> b.getIsbn().equalsIgnoreCase(isbn))
                .findFirst()
                .orElseThrow(() -> new BookNotFoundException(isbn));

        book.setRating(rating);
        userRepository.save(user);
        return book;
    }

    /**
     * Ruft ein bestimmtes Buch eines Benutzers anhand der ISBN ab.
     *
     * @param userId Die ID des Benutzers
     * @param isbn   Die ISBN des Buches
     * @return Das gefundene Buch
     * @throws UserNotFoundException Wenn der Benutzer nicht existiert
     * @throws BookNotFoundException Wenn kein Buch mit der angegebenen ISBN gefunden wurde
     */
    public Book getBookByUserIdAndIsbn(UUID userId, String isbn) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        return user.getBooks().stream()
                .filter(book -> book.getIsbn().equalsIgnoreCase(isbn))
                .findFirst()
                .orElseThrow(() -> new BookNotFoundException(isbn));
    }

    /**
     * Löscht ein Buch aus der Sammlung eines Benutzers anhand der ISBN.
     *
     * @param userId Die Benutzer-ID
     * @param isbn   Die ISBN des Buchs
     * @throws UserNotFoundException Wenn der Benutzer nicht existiert
     * @throws BookNotFoundException Wenn das Buch nicht existiert oder nicht dem Benutzer zugeordnet ist
     */
    public void deleteBookByUserIdAndIsbn(UUID userId, String isbn) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        Book book = user.getBooks().stream()
                .filter(b -> b.getIsbn().equalsIgnoreCase(isbn))
                .findFirst()
                .orElseThrow(() -> new BookNotFoundException(isbn));

        user.getBooks().remove(book);
        bookRepository.delete(book);
    }

    /**
     * Gibt alle Bücher eines Benutzers zurück, optional gefiltert nach Bewertung.
     *
     * @param userId Benutzer-ID
     * @param rating (optional) Bewertung zum Filtern, darf zwischen 1 und 5 liegen
     * @return Liste der passenden Bücher
     * @throws UserNotFoundException wenn Benutzer nicht existiert
     * @throws InvalidBookException  bei ungültigem Rating-Parameter
     */
    public List<Book> getBooksByUserIdAndOptionalRating(UUID userId, Integer rating) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        if (rating != null) {
            if (rating < 1 || rating > 5) {
                throw new InvalidBookException("Bewertung muss zwischen 1 und 5 liegen.");
            }
            return user.getBooks().stream()
                    .filter(book -> book.getRating() != null && book.getRating() == rating)
                    .toList();
        }
        return user.getBooks();
    }

    /**
     * Sucht Bücher eines Benutzers anhand optionaler Kriterien.
     *
     * @param userId Benutzer-ID
     * @param title Optionaler Titel
     * @param author Optionaler Autor
     * @param year Optionales Jahr
     * @return Liste der passenden Bücher
     */
    public List<Book> searchBooks(UUID userId, String title, String author, Integer year) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        return user.getBooks().stream()
                .filter(book -> title == null || book.getTitle() != null && book.getTitle().toLowerCase().contains(title.toLowerCase()))
                .filter(book -> author == null || book.getAuthors() != null && book.getAuthors().stream().anyMatch(a -> a.toLowerCase().contains(author.toLowerCase())))
                .filter(book -> year == null || (book.getPublishedDate() != null && book.getPublishedDate().contains(year.toString())))
                .toList();
    }

}