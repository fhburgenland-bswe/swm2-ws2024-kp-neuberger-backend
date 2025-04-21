package at.fhburgenland.bookmanager.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Datenklasse (DTO) zur Übergabe einer ISBN bei der Bucherstellung.
 *
 * Wird z. B. im Request-Body verwendet, um ein Buch anhand seiner ISBN über die OpenLibrary API hinzuzufügen.
 */
public class IsbnRequest {

    /**
     * Die ISBN des Buches. Dieses Feld darf nicht leer sein.
     */
    @NotBlank(message = "Die ISBN darf nicht leer sein")
    final private String isbn;

    /**
     * Erstellt ein neues IsbnRequest-Objekt mit der angegebenen ISBN.
     *
     * @param isbn Die zu verwendende ISBN.
     */
    public IsbnRequest(String isbn) {
        this.isbn = isbn;
    }

    /**
     * Gibt die ISBN zurück.
     *
     * @return Die gespeicherte ISBN.
     */
    public String getIsbn() {
        return isbn;
    }
}
