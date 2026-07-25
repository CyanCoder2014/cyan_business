package com.cyancoder.dynamiccore.runtime;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

public record DynamicPageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public static <S, T> DynamicPageResponse<T> from(Page<S> source, Function<S, T> mapper) {
        return new DynamicPageResponse<>(
                source.getContent().stream().map(mapper).toList(),
                source.getNumber(),
                source.getSize(),
                source.getTotalElements(),
                source.getTotalPages()
        );
    }
}
