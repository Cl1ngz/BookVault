package com.bookvault.library.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "ksiazki", schema = "biblioteka")
@Data
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ksiazki")
    private Integer idKsiazki;

    @Column(nullable = false)
    private String tytul;

    @ManyToOne
    @JoinColumn(name = "id_autora", nullable = false)
    private Author author;

    @ManyToOne
    @JoinColumn(name = "id_wydawnictwa")
    private Publisher publisher;

    @Column(name = "id_serii")
    private Integer idSerii; // Mapujemy jako Integer

    @ManyToMany
    @JoinTable(
            name = "ksiazka_gatunek",
            schema = "biblioteka",
            joinColumns = @JoinColumn(name = "id_ksiazki"),
            inverseJoinColumns = @JoinColumn(name = "id_gatunku")
    )
    private Set<Genre> genres = new HashSet<>();

    @Column(name = "rok_wydania")
    private Integer rokWydania;

    @Column(name = "ilosc_stron")
    private Integer iloscStron;

    @Column(name = "nastroj")
    private String mood;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}