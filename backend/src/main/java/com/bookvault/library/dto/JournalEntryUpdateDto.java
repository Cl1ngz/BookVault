package com.bookvault.library.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

public class JournalEntryUpdateDto {

    @PastOrPresent(message = "Data wpisu nie może być z przyszłości")
    private LocalDate entryDate;

    @Min(value = 0, message = "Liczba przeczytanych stron nie może być ujemna")
    private Integer cumulativePages;

    public LocalDate getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(LocalDate entryDate) {
        this.entryDate = entryDate;
    }

    public Integer getCumulativePages() {
        return cumulativePages;
    }

    public void setCumulativePages(Integer cumulativePages) {
        this.cumulativePages = cumulativePages;
    }
}