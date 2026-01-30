package com.kratos.mok.pricing.shared.api;

import org.springframework.data.domain.Page;
import java.util.List;

public record PageResponseDto<T>(
        List<T> items,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious
) {
    public static <T> PageResponseDto<T> from(org.springframework.data.domain.Page<T> p) {
        return new PageResponseDto<>(
                p.getContent(),
                p.getNumber(),
                p.getSize(),
                p.getTotalElements(),
                p.getTotalPages(),
                p.hasNext(),
                p.hasPrevious()
        );
    }
}
