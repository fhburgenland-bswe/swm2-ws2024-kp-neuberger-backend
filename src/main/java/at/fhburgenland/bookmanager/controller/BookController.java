// src/main/java/at/fhburgenland/bookmanager/controller/BookController.java
package at.fhburgenland.bookmanager.controller;

import at.fhburgenland.bookmanager.dto.IsbnRequest;
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
}
