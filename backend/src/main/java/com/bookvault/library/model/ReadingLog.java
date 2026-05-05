package com.bookvault.library.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "reading_log", schema = "biblioteka")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ReadingLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reading_log")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_czytelnika", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "passwordHash"})
    private Reader reader;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_ksiazki", nullable = false)
    @JsonIgnoreProperties({"reviews", "hibernateLazyInitializer", "handler"})
    private Book book;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ReadingStatus status = ReadingStatus.TO_READ;

    @Column(name = "strony_przeczytane")
    private Integer pagesRead = 0;

    @Column(name = "data_rozpoczecia")
    private LocalDate startedAt;

    @Column(name = "data_zakonczenia")
    private LocalDate finishedAt;
}

