package com.bookvault.library.dto;

import com.bookvault.library.model.ReadingStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

public class ReadingLogUpdateDto {

    private ReadingStatus status;

    @Min(value = 0, message = "Liczba przeczytanych stron nie może być ujemna")
    private Integer pagesRead;

    @PastOrPresent(message = "Data rozpoczęcia nie może być z przyszłości")
    private LocalDate startedAt;

    @PastOrPresent(message = "Data zakończenia nie może być z przyszłości")
    private LocalDate finishedAt;

    public ReadingStatus getStatus() {
        return status;
    }

    public void setStatus(ReadingStatus status) {
        this.status = status;
    }

    public Integer getPagesRead() {
        return pagesRead;
    }

    public void setPagesRead(Integer pagesRead) {
        this.pagesRead = pagesRead;
    }

    public LocalDate getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDate startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDate getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(LocalDate finishedAt) {
        this.finishedAt = finishedAt;
    }
}