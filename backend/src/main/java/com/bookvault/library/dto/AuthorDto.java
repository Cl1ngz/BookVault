package com.bookvault.library.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class AuthorDto {

    @NotBlank(message = "Imię autora nie może być puste")
    @Size(min = 2, max = 100, message = "Imię autora musi mieć od 2 do 100 znaków")
    private String firstName;

    @NotBlank(message = "Nazwisko autora nie może być puste")
    @Size(min = 2, max = 100, message = "Nazwisko autora musi mieć od 2 do 100 znaków")
    private String lastName;

    @Past(message = "Data urodzenia musi być datą z przeszłości")
    private LocalDate birthDate;

    @Size(min = 2, max = 50, message = "Narodowość musi mieć od 2 do 50 znaków")
    private String nationality;

    @Size(min = 10, max = 5000, message = "Biografia musi mieć od 10 do 5000 znaków")
    private String biography;

    @Email(message = "Adres e-mail ma niepoprawny format")
    @Size(max = 255, message = "Adres e-mail może mieć maksymalnie 255 znaków")
    private String email;
}
