package com.example.biblioteka.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "ksiazki", schema = "biblioteka")
@Data
public class Ksiazka {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ksiazki")
    private Integer idKsiazki;

    @Column(nullable = false)
    private String tytul;

    @ManyToOne
    @JoinColumn(name = "id_autora", nullable = false)
    private Autor autor;

    @ManyToOne
    @JoinColumn(name = "id_wydawnictwa")
    private Wydawnictwo wydawnictwo;

    @Column(name = "id_serii")
    private Integer idSerii; // Mapujemy jako Integer

    @Column(name = "rok_wydania")
    private Integer rokWydania;

    @Column(name = "ilosc_stron")
    private Integer iloscStron;

    private String nastroj;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}