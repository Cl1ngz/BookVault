package com.bookvault.library.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "czytelnicy", schema = "biblioteka")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Reader extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_czytelnika")
    private Integer id;

    @Column(name = "imie", nullable = false, length = 100) // Dodano length
    private String firstName;

    @Column(name = "nazwisko", nullable = false, length = 100) // Dodano length
    private String lastName;

    @Column(name = "data_urodzenia")
    private LocalDate birthDate;

    @Column(name = "narodowosc", length = 50) // Dodano length
    private String nationality;
}