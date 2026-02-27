package edu.eci.arsw.blueprints;

import edu.eci.arsw.blueprints.filters.IdentityFilter;
import edu.eci.arsw.blueprints.filters.RedundancyFilter;
import edu.eci.arsw.blueprints.filters.UndersamplingFilter;
import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BlueprintsSmokeTest {

    @Test
    @DisplayName("El contexto de Spring carga correctamente")
    void contextLoads() {}

    @Nested
    @DisplayName("RedundancyFilter")
    class RedundancyFilterTests {

        private RedundancyFilter filter;

        @BeforeEach
        void setUp() { filter = new RedundancyFilter(); }

        @Test
        @DisplayName("Elimina puntos consecutivos duplicados")
        void removesConsecutiveDuplicates() {
            Blueprint bp = new Blueprint("autor", "plano", List.of(
                    new Point(0, 0), new Point(0, 0),
                    new Point(5, 5), new Point(5, 5), new Point(3, 1)
            ));
            Blueprint result = filter.apply(bp);
            assertEquals(List.of(new Point(0, 0), new Point(5, 5), new Point(3, 1)),
                    result.getPoints());
        }

        @Test
        @DisplayName("No elimina puntos iguales no consecutivos")
        void keepsNonConsecutiveDuplicates() {
            Blueprint bp = new Blueprint("autor", "plano", List.of(
                    new Point(1, 1), new Point(2, 2), new Point(1, 1)
            ));
            Blueprint result = filter.apply(bp);
            assertEquals(3, result.getPoints().size());
        }

        @Test
        @DisplayName("Devuelve el blueprint sin cambios si está vacío")
        void emptyBlueprintUnchanged() {
            Blueprint bp = new Blueprint("autor", "plano", List.of());
            Blueprint result = filter.apply(bp);
            assertTrue(result.getPoints().isEmpty());
        }

        @Test
        @DisplayName("Un solo punto no sufre cambios")
        void singlePointUnchanged() {
            Blueprint bp = new Blueprint("autor", "plano", List.of(new Point(3, 3)));
            Blueprint result = filter.apply(bp);
            assertEquals(1, result.getPoints().size());
        }
    }

    @Nested
    @DisplayName("UndersamplingFilter")
    class UndersamplingFilterTests {

        private UndersamplingFilter filter;

        @BeforeEach
        void setUp() { filter = new UndersamplingFilter(); }

        @Test
        @DisplayName("Conserva solo los puntos en índices pares")
        void keepEvenIndexPoints() {
            Blueprint bp = new Blueprint("autor", "plano", List.of(
                    new Point(0, 0), new Point(1, 1),
                    new Point(2, 2), new Point(3, 3), new Point(4, 4)
            ));
            Blueprint result = filter.apply(bp);
            assertEquals(List.of(new Point(0, 0), new Point(2, 2), new Point(4, 4)),
                    result.getPoints());
        }

        @Test
        @DisplayName("Con 2 puntos o menos devuelve el blueprint sin cambios")
        void twoPointsOrLessUnchanged() {
            Blueprint bp = new Blueprint("autor", "plano", List.of(
                    new Point(0, 0), new Point(1, 1)
            ));
            Blueprint result = filter.apply(bp);
            assertEquals(2, result.getPoints().size());
        }

        @Test
        @DisplayName("Con un punto devuelve el blueprint sin cambios")
        void singlePointUnchanged() {
            Blueprint bp = new Blueprint("autor", "plano", List.of(new Point(7, 7)));
            Blueprint result = filter.apply(bp);
            assertEquals(1, result.getPoints().size());
        }
    }
    @Nested
    @DisplayName("IdentityFilter")
    class IdentityFilterTests {

        private IdentityFilter filter;

        @BeforeEach
        void setUp() { filter = new IdentityFilter(); }

        @Test
        @DisplayName("Devuelve exactamente el mismo objeto blueprint")
        void returnsSameBlueprint() {
            Blueprint bp = new Blueprint("autor", "plano", List.of(new Point(1, 2)));
            assertSame(bp, filter.apply(bp));
        }

        @Test
        @DisplayName("No modifica la lista de puntos")
        void pointsUnchanged() {
            Blueprint bp = new Blueprint("autor", "plano", List.of(
                    new Point(0, 0), new Point(5, 5)
            ));
            assertEquals(bp.getPoints(), filter.apply(bp).getPoints());
        }
    }
}
