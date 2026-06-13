package com.bookvault.library.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewReportDto {

    @NotNull(message = "Id recenzji jest wymagane")
    private Integer reviewId;

    @NotBlank(message = "Powód zgłoszenia nie może być pusty")
    @Size(min = 5, max = 1000, message = "Powód zgłoszenia musi mieć od 5 do 1000 znaków")
    private String reason;
}
