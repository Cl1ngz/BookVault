package com.bookvault.library.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class BookUpdateDto {

    @Size(min = 2, max = 255, message = "Tytuł książki musi mieć od 2 do 255 znaków")
    private String title;

    private Integer authorId;

    private Integer publisherId;

    private Integer seriesId;

    @Min(value = 1000, message = "Rok wydania nie może być mniejszy niż 1000")
    @Max(value = 2100, message = "Rok wydania nie może być większy niż 2100")
    private Integer publicationYear;

    @Min(value = 1, message = "Liczba stron musi być większa od 0")
    @Max(value = 10000, message = "Liczba stron jest zbyt duża")
    private Integer pageCount;

    @Size(min = 2, max = 50, message = "Nastrój musi mieć od 2 do 50 znaków")
    private String mood;

    private Set<Integer> genreIds;
}
