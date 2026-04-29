package com.bookvault.library.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "autorzy")
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Author extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_autora")
    private Integer id;

    @Column(name = "imie", nullable = false, length = 100)
    private String firstName;

    @Column(name = "nazwisko", nullable = false, length = 100)
    private String lastName;

    @Column(name = "data_urodzenia")
    private LocalDate birthDate;

    @Column(name = "narodowosc", length = 50)
    private String nationality;

    @Column(name = "biografia", columnDefinition = "TEXT")
    private String biography;

    @Column(name = "email", unique = true, length = 255)
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "password_hash", length = 255)
    private String passwordHash;
}
