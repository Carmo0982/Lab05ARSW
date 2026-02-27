package edu.eci.arsw.blueprints.persistence;
import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Repository
@Primary
public class PostgresBlueprintPersistence implements BlueprintPersistence{
    private final BlueprintRepository blueprintRepository;

    public PostgresBlueprintPersistence(BlueprintRepository blueprintRepository) {
        this.blueprintRepository = blueprintRepository;
    }

    @Override
    public void saveBlueprint(Blueprint bp) throws BlueprintPersistenceException {
        Optional<Blueprint> existing = blueprintRepository.findByAuthorAndName(bp.getAuthor(), bp.getName());
        if (existing.isPresent()) throw new BlueprintPersistenceException("Blueprint ya exíste");
        blueprintRepository.save(bp);
    }

    @Override
    public Blueprint getBlueprint(String author, String name) throws BlueprintNotFoundException {
        Optional<Blueprint> existing = blueprintRepository.findByAuthorAndName(author,name);
        if(existing.isEmpty()) throw new BlueprintNotFoundException("Blueprint no encontrado");
        return existing.get();
    }

    @Override
    public Set<Blueprint> getBlueprintsByAuthor(String author) throws BlueprintNotFoundException {
        Set<Blueprint> existing = blueprintRepository.findByAuthor(author);
        if (existing.isEmpty()) throw new BlueprintNotFoundException("Blueprint no encontrado");
        return existing;
    }

    @Override
    public Set<Blueprint> getAllBlueprints() {
        return new HashSet<>(blueprintRepository.findAll());
    }

    @Override
    public void addPoint(String author, String name, int x, int y) throws BlueprintNotFoundException {
        Optional<Blueprint> existing = blueprintRepository.findByAuthorAndName(author,name);
        if (existing.isEmpty()) throw new BlueprintNotFoundException("Blueprint no encontrado");
        Blueprint bd = existing.get();
        bd.addPoint(new Point(x,y));
        blueprintRepository.save(bd);

    }
}
