package com.bookvault.library.dto;

import com.bookvault.library.model.ReadingStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReadingLogCreateDto {

    @NotNull(message = "Id książki jest wymagane")
    private Integer bookId;

    private ReadingStatus status = ReadingStatus.TO_READ;
}
