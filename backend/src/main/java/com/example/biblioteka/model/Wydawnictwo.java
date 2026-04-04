package com.example.biblioteka.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "wydawnictwa", schema = "biblioteka")
@Data
public class Wydawnictwo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_wydawnictwa")
    private Integer idWydawnictwa;

    @Column(nullable = false)
    private String nazwa;

    @Column(name = "id_adresu")
    private Integer idAdresu; // Klucz obcy jako zwykły Integer, dopóki nie zmapujemy adresów

    @Column(name = "rok_zalozenia")
    private Integer rokZalozenia;

    private String wlasciciel;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}