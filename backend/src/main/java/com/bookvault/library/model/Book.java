package com.bookvault.library.model;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "ksiazki")
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

    @Column(name = "tytul", nullable = false)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_autora")
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

    @Column(name = "nastroj")
    private String mood;

    @ManyToMany
    @JoinTable(
            name = "ksiazka_gatunek",
            joinColumns = @JoinColumn(name = "id_ksiazki"),
            inverseJoinColumns = @JoinColumn(name = "id_gatunku")
    )
    private Set<Genre> genres = new HashSet<>();
}