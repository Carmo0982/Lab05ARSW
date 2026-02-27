package edu.eci.arsw.blueprints.persistence;

import edu.eci.arsw.blueprints.model.Blueprint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.Set;

public interface BlueprintRepository extends JpaRepository<Blueprint,Long> {
    Optional<Blueprint> findByAuthorAndName(String author, String name);
    Set<Blueprint> findByAuthor(String author);
}
