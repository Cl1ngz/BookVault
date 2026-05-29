package com.bookvault.library.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ReviewDto {

    @NotNull(message = "Id książki jest wymagane")
    private Integer bookId;

    @NotNull(message = "Ocena jest wymagana")
    @DecimalMin(value = "0.25", message = "Ocena musi być co najmniej 0.25")
    @DecimalMax(value = "5.0", message = "Ocena może wynosić maksymalnie 5.0")
    private Double rating;

    @Size(max = 5000, message = "Treść recenzji może mieć maksymalnie 5000 znaków")
    private String content;

    public Integer getBookId() {
        return bookId;
    }

    public void setBookId(Integer bookId) {
        this.bookId = bookId;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}