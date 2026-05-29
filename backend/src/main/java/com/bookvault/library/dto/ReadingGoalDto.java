package com.bookvault.library.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class ReadingGoalDto {

    @Min(value = 1900, message = "Rok jest zbyt mały")
    @Max(value = 2100, message = "Rok jest zbyt duży")
    private Integer year = LocalDate.now().getYear();

    @NotNull(message = "Cel liczby książek jest wymagany")
    @Min(value = 1, message = "Cel musi być większy od 0")
    @Max(value = 1000, message = "Cel jest zbyt duży")
    private Integer targetBooks;

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getTargetBooks() {
        return targetBooks;
    }

    public void setTargetBooks(Integer targetBooks) {
        this.targetBooks = targetBooks;
    }
}