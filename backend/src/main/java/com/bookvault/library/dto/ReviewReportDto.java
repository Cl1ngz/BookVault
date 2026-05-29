package com.bookvault.library.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ReviewReportDto {

    @NotNull(message = "Id recenzji jest wymagane")
    private Integer reviewId;

    @NotBlank(message = "Powód zgłoszenia nie może być pusty")
    @Size(min = 5, max = 1000, message = "Powód zgłoszenia musi mieć od 5 do 1000 znaków")
    private String reason;

    public Integer getReviewId() {
        return reviewId;
    }

    public void setReviewId(Integer reviewId) {
        this.reviewId = reviewId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}