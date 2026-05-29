package com.bookvault.library.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class ReaderUpdateDto {

    @Size(min = 3, max = 100, message = "Nazwa użytkownika musi mieć od 3 do 100 znaków")
    private String username;

    @Email(message = "Adres e-mail ma niepoprawny format")
    @Size(max = 255, message = "Adres e-mail może mieć maksymalnie 255 znaków")
    private String email;

    @Past(message = "Data urodzenia musi być datą z przeszłości")
    private LocalDate birthDate;

    @Size(max = 50, message = "Narodowość może mieć maksymalnie 50 znaków")
    private String nationality;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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
}