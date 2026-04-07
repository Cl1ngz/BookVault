package com.bookvault.library.model;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "gatunki") // Mapowanie na polską nazwę tabeli
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@NoArgsConstructor
@AllArgsConstructor
public class Genre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_gatunku") // Mapowanie polskiej nazwy ID
    private Integer id;

    @Column(name = "nazwa", nullable = false, unique = true) // Mapowanie 'nazwa' na 'name'
    private String name;
}