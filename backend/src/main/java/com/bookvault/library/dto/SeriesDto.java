package com.bookvault.library.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SeriesDto {

    @NotBlank(message = "Nazwa serii nie może być pusta")
    @Size(min = 2, max = 150, message = "Nazwa serii musi mieć od 2 do 150 znaków")
    private String name;

    @Min(value = 1, message = "Liczba tomów musi być większa od 0")
    @Max(value = 1000, message = "Liczba tomów jest zbyt duża")
    private Integer volumeCount;

    private Integer authorId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getVolumeCount() {
        return volumeCount;
    }

    public void setVolumeCount(Integer volumeCount) {
        this.volumeCount = volumeCount;
    }

    public Integer getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Integer authorId) {
        this.authorId = authorId;
    }
}