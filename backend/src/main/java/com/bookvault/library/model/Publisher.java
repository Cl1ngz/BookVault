package com.bookvault.library.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "wydawnictwa", schema = "biblioteka") // Dodany schema dla pewności
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Publisher extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_wydawnictwa")
    private Integer id;

    @Column(name = "nazwa", nullable = false, length = 150) // Dodany length z SQL
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_adresu") // SQL nie ma NOT NULL, więc zostaje domyślne nullable
    private Address address;

    @Column(name = "rok_zalozenia")
    private Integer foundationYear;

    @Column(name = "wlasciciel", length = 150) // Dodany length z SQL
    private String owner;
}