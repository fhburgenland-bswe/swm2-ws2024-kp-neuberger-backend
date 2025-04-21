package at.fhburgenland.bookmanager.repository;

import at.fhburgenland.bookmanager.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository zur Verwaltung von Benutzer-Entitäten.
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {

    /**
     * Sucht nach einem Benutzer anhand seiner E-Mail-Adresse.
     *
     * @param email E-Mail-Adresse
     * @return Optional mit Benutzer (wenn gefunden)
     */
    Optional<User> findByEmail(String email);
}
