package com.bookvault.library.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public class ReaderDto {

    private Integer id;

    @NotBlank(message = "Nazwa użytkownika nie może być pusta")
    @Size(min = 3, max = 50, message = "Nazwa użytkownika musi zawierać od 3 do 50 znaków")
    private String username;

    @NotBlank(message = "Email jest wymagany")
    @Email(message = "Podano niepoprawny format adresu email")
    private String email;

    @NotNull(message = "Data urodzenia jest wymagana")
    @Past(message = "Data urodzenia musi być datą z przeszłości")
    private LocalDate birthDate;

    @NotBlank(message = "Narodowość nie może być pusta")
    private String nationality;

    private String role;

    public ReaderDto() {
    }

    public ReaderDto(Integer id, String username, String email, LocalDate birthDate, String nationality, String role) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.birthDate = birthDate;
        this.nationality = nationality;
        this.role = role;
    }

    public Integer getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public String getNationality() {
        return nationality;
    }

    public String getRole() {
        return role;
    }
}
