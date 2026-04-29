package com.bookvault.library.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "ksiazki", schema = "biblioteka") // Dodany schema dla pewności
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Book extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ksiazki")
    private Integer id;

    @Column(name = "tytul", nullable = false, length = 255) // Dodany length z SQL
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_autora") // SQL nie ma NOT NULL, więc zostawiamy domyślne nullable
    private Author author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_wydawnictwa")
    private Publisher publisher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_serii")
    private Series series;

    @Column(name = "rok_wydania")
    private Integer publicationYear;

    @Column(name = "ilosc_stron")
    private Integer pageCount;

    @Column(name = "nastroj", length = 50) // Dodany length z SQL
    private String mood;

    @ManyToMany
    @JoinTable(
            name = "ksiazka_gatunek",
            schema = "biblioteka", // Spójność z tabelą łączącą
            joinColumns = @JoinColumn(name = "id_ksiazki"),
            inverseJoinColumns = @JoinColumn(name = "id_gatunku")
    )
    private Set<Genre> genres = new HashSet<>(); // Inicjalizacja HashSetem!
}