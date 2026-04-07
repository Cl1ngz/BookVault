package com.bookvault.library.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "serie", schema = "biblioteka")
@Data
public class Series {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_serii")
    private Integer idSerii;

    @Column(nullable = false)
    private String nazwa;

    @Column(name = "liczba_tomow")
    private Short liczbaTomow;

    @ManyToOne
    @JoinColumn(name = "id_autora")
    private Author author;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}


