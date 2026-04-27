package com.bookvault.library.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "serie", schema = "biblioteka")
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Series extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_serii")
    private Integer id;

    @Column(name = "nazwa", nullable = false, length = 150) // Dodano length z SQL
    private String name;

    @Column(name = "liczba_tomow")
    private Short volumeCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_autora")
    private Author author;
}