package com.bookvault.library.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "wpisy_dziennika", schema = "biblioteka")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class JournalEntry extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_wpisu")
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_reading_log", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "reader"})
    private ReadingLog readingLog;

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false, length = 20)
    private JournalEntryType entryType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private ReadingStatus status;

    @Column(name = "strony_laczne")
    private Integer cumulativePages;

    @Column(name = "data_wpisu")
    private LocalDate entryDate = LocalDate.now();
}
