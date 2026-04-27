package com.bookvault.library.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "recenzje", schema = "biblioteka") // Dodany schema dla spójności
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
// Mapujemy pole createdAt z BaseEntity na kolumnę data_dodania z SQL
@AttributeOverride(name = "createdAt", column = @Column(name = "data_dodania"))
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_recenzji")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ksiazki", nullable = false) // W SQL jest kluczem obcym, musi być przypisany
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_czytelnika", nullable = false) // Recenzja musi mieć autora (czytelnika)
    private Reader reader;

    @Column(name = "ocena", nullable = false)
    private Integer rating;

    @Column(name = "tresc", columnDefinition = "TEXT")
    private String content;
}