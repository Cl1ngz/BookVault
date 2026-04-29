package com.bookvault.library.model;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    @Column(name = "username", nullable = false, length = 100)
    private String username;

    @Column(name = "email", unique = true, length = 255)
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Column(name = "data_urodzenia")
    private LocalDate birthDate;

    @Column(name = "narodowosc", length = 50)
    private String nationality;
}
