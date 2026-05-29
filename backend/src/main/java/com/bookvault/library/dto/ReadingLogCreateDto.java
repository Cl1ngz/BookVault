package com.bookvault.library.dto;

import com.bookvault.library.model.ReadingStatus;
import jakarta.validation.constraints.NotNull;

public class ReadingLogCreateDto {

    @NotNull(message = "Id książki jest wymagane")
    private Integer bookId;

    private ReadingStatus status = ReadingStatus.TO_READ;

    public Integer getBookId() {
        return bookId;
    }

    public void setBookId(Integer bookId) {
        this.bookId = bookId;
    }

    public ReadingStatus getStatus() {
        return status;
    }

    public void setStatus(ReadingStatus status) {
        this.status = status;
    }
}