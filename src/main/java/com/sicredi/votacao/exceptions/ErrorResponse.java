package com.sicredi.votacao.exceptions;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record ErrorResponse(String timestamp, int status, String error, String message) {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public static ErrorResponse of(int status, String error, String message) {
        return new ErrorResponse(LocalDateTime.now().format(FORMATTER), status, error, message);
    }
}
