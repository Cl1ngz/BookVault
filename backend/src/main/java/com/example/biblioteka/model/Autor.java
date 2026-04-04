package com.example.biblioteka.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "autorzy", schema = "biblioteka")
@Data // gettery, settery, toString, equals i hashCode
@NoArgsConstructor
@AllArgsConstructor
public class Autor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_autora") //mapowanie nazwy snake_case na camelCase
    private Integer idAutora;

    @Column(nullable = false)
    private String imie;

    @Column(nullable = false)
    private String nazwisko;

    @Column(name = "data_urodzenia")
    private LocalDate dataUrodzenia;

    private String narodowosc;

    @Column(columnDefinition = "TEXT")
    private String biografia;

    //pola audytowe stworzone w sql
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}