// src/main/java/at/fhburgenland/bookmanager/controller/BookController.java
package at.fhburgenland.bookmanager.controller;

import at.fhburgenland.bookmanager.dto.IsbnRequest;
import at.fhburgenland.bookmanager.dto.RatingUpdateRequest;
import at.fhburgenland.bookmanager.model.Book;
import at.fhburgenland.bookmanager.service.BookService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST-Controller für das hinzufügen eines Buches zu einem Benutzer.
 */
@RestController
@RequestMapping("/users/{userId}/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    /**
     * Fügt dem Benutzer ein Buch hinzu, indem im Body JSON mit {"isbn":"..."} übergeben wird.
     *
     * @param userId       die UUID des bestehenden Benutzers
     * @param isbnRequest  JSON‑DTO mit dem Feld "isbn"
     * @return 201 Created mit dem neuen Book‑Objekt, oder 400/404/500 über GlobalExceptionHandler
     */
    @PostMapping
    public ResponseEntity<Book> addBookByIsbn(
            @PathVariable("userId") UUID userId,
            @Valid @RequestBody IsbnRequest isbnRequest
    ) {
        Book created = bookService.addBookToUserByIsbn(userId, isbnRequest.getIsbn());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Aktualisiert nur die Bewertung eines vorhandenen Buchs für einen Benutzer.
     *
     * @param userId die ID des Benutzers
     * @param isbn   die ISBN des Buches
     * @param request JSON‑Objekt mit dem neuen Bewertungswert (rating)
     * @return das aktualisierte Buchobjekt mit HTTP 200, oder HTTP 400/404
     */
    @PutMapping("/{isbn}")
    public ResponseEntity<Book> updateBookRating(
            @PathVariable UUID userId,
            @PathVariable String isbn,
            @Valid @RequestBody RatingUpdateRequest request
    ) {
        Book updated = bookService.updateBookRating(userId, isbn, request.getRating());
        return ResponseEntity.ok(updated);
    }

    /**
     * Liefert die vollständigen Details eines Buches für einen bestimmten Benutzer.
     *
     * @param userId ID des Benutzers
     * @param isbn   ISBN des Buches
     * @return HTTP 200 mit dem Buchobjekt oder HTTP 404, wenn nicht vorhanden
     */
    @GetMapping("/{isbn}")
    public ResponseEntity<Book> getBookDetails(
            @PathVariable UUID userId,
            @PathVariable String isbn
    ) {
        Book book = bookService.getBookByUserIdAndIsbn(userId, isbn);
        return ResponseEntity.ok(book);
    }

    /**
     * Löscht ein Buch anhand der ISBN für einen bestimmten Benutzer.
     *
     * @param userId Die ID des Benutzers
     * @param isbn   Die ISBN des zu löschenden Buches
     * @return HTTP 204 bei Erfolg, 404 bei Nichtfinden
     */
    @DeleteMapping("/{isbn}")
    public ResponseEntity<Void> deleteBook(
            @PathVariable("userId") UUID userId,
            @PathVariable("isbn") String isbn
    ) {
        bookService.deleteBookByUserIdAndIsbn(userId, isbn);
        return ResponseEntity.noContent().build();
    }

    /**
     * Gibt die Liste der Bücher eines Benutzers zurück, optional gefiltert nach Bewertung.
     *
     * @param userId Die ID des Benutzers
     * @param rating (Optional) Bewertungsfilter (1-5)
     * @return Liste der Bücher
     */
    @GetMapping
    public ResponseEntity<List<Book>> getBooks(@PathVariable UUID userId,
                                               @RequestParam(required = false) Integer rating) {
        List<Book> books = bookService.getBooksByUserIdAndOptionalRating(userId, rating);
        return ResponseEntity.ok(books);
    }

    /**
     * Sucht Bücher eines Benutzers anhand von optionalen Kriterien.
     *
     * @param userId ID des Benutzers
     * @param title Optionaler Filtern nach Titel
     * @param author Optionaler Filtern nach Autor
     * @param year Optionaler Filtern nach Veröffentlichungsjahr
     * @return Gefilterte Liste von Büchern
     */
    @GetMapping("/search")
    public ResponseEntity<List<Book>> searchBooks(
            @PathVariable String userId,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) Integer year
    ) {
        UUID uuid;
        try {
            uuid = UUID.fromString(userId);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }

        List<Book> books = bookService.searchBooks(uuid, title, author, year);
        return ResponseEntity.ok(books);
    } 
}
