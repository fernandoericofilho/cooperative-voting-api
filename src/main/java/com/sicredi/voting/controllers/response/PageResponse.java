package com.sicredi.voting.controllers.response;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

public record PageResponse<T>(
    @JsonProperty("content")
    List<T> content,

    @JsonProperty("totalElements")
    long totalElements,

    @JsonProperty("totalPages")
    int totalPages,

    @JsonProperty("currentPage")
    int currentPage,

    @JsonProperty("pageSize")
    int pageSize,

    @JsonProperty("hasNext")
    boolean hasNext,

    @JsonProperty("hasPrevious")
    boolean hasPrevious
) {
}
