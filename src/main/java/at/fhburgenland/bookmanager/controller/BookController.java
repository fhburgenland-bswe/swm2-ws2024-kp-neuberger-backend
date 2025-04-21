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
}
