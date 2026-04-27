package com.bookvault.library.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "adresy")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Address extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_adresu")
    private Integer id;

    @Column(name = "ulica", length = 100)
    private String street;

    @Column(name = "numer_domu", nullable = false, length = 10)
    private String houseNumber;

    @Column(name = "miasto", nullable = false, length = 100)
    private String city;

    @Column(name = "kod_pocztowy", nullable = false, length = 10)
    private String zipCode;

    @Column(name = "kraj", nullable = false, length = 100)
    private String country;
}