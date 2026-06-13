package com.bookvault.library.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GenreDto {

    @NotBlank(message = "Nazwa gatunku nie może być pusta")
    @Size(min = 2, max = 100, message = "Nazwa gatunku musi mieć od 2 do 100 znaków")
    private String name;
}
