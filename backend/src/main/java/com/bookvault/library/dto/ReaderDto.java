package com.bookvault.library.dto;

import java.time.LocalDate;

public class ReaderDto {

    private Integer id;
    private String username;
    private String email;
    private LocalDate birthDate;
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