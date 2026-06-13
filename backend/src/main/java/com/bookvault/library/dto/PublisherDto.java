package com.bookvault.library.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PublisherDto {

    @NotBlank(message = "Nazwa wydawnictwa nie może być pusta")
    @Size(min = 2, max = 150, message = "Nazwa wydawnictwa musi mieć od 2 do 150 znaków")
    private String name;

    @Min(value = 1400, message = "Rok założenia jest zbyt mały (minimum 1400)")
    @Max(value = 2100, message = "Rok założenia jest zbyt duży (maksimum 2100)")
    private Integer foundationYear;

    @Size(min = 2, max = 150, message = "Właściciel musi mieć od 2 do 150 znaków")
    private String owner;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getFoundationYear() {
        return foundationYear;
    }

    public void setFoundationYear(Integer foundationYear) {
        this.foundationYear = foundationYear;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }
}
