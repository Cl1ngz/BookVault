package com.bookvault.library.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "zgloszenia_recenzji", schema = "biblioteka")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ReviewReport extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_zgloszenia")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_recenzji", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Review review;

    @Column(name = "reporter_type", nullable = false, length = 10)
    private String reporterType; // "reader" or "author"

    @Column(name = "reporter_id", nullable = false)
    private Integer reporterId;

    @Column(name = "reason", nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(name = "status", length = 20)
    private String status = "pending"; // pending, resolved, dismissed
}
