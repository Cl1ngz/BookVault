package com.bookvault.library.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class AuthorDto {

    @NotBlank(message = "Imię autora nie może być puste")
    @Size(min = 2, max = 100, message = "Imię autora musi mieć od 2 do 100 znaków")
    private String firstName;

    @NotBlank(message = "Nazwisko autora nie może być puste")
    @Size(min = 2, max = 100, message = "Nazwisko autora musi mieć od 2 do 100 znaków")
    private String lastName;

    @Past(message = "Data urodzenia musi być datą z przeszłości")
    private LocalDate birthDate;

    @Size(max = 50, message = "Narodowość może mieć maksymalnie 50 znaków")
    private String nationality;

    @Size(max = 5000, message = "Biografia może mieć maksymalnie 5000 znaków")
    private String biography;

    @Email(message = "Adres e-mail ma niepoprawny format")
    @Size(max = 255, message = "Adres e-mail może mieć maksymalnie 255 znaków")
    private String email;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getBiography() {
        return biography;
    }

    public void setBiography(String biography) {
        this.biography = biography;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}