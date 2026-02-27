package edu.eci.arsw.blueprints.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
@Entity
@Table(name = "blueprints", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"author", "name"})
})
public class Blueprint {
    //Lo más simple
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String author;
    private String name;
    @ElementCollection
    @CollectionTable(name = "blueprint_points", joinColumns = @JoinColumn(name = "blueprint_id"))
    private List<Point> points = new ArrayList<>();

    public Blueprint(String author, String name, List<Point> pts) {
        this.author = author;
        this.name = name;
        if (pts != null) points.addAll(pts);
    }
    public Blueprint(){}
    public String getAuthor() { return author; }
    public String getName() { return name; }
    public List<Point> getPoints() { return Collections.unmodifiableList(points); }
    public Long getId() {return id;}

    public void addPoint(Point p) { points.add(p); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Blueprint bp)) return false;
        return Objects.equals(author, bp.author) && Objects.equals(name, bp.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(author, name);
    }

}
