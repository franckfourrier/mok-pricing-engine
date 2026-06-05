package com.kratos.mok.pricing.shared.api;

public record PageableRequest(
        int page,
        int size
) {

    public static PageableRequest of(Integer page, Integer size) {
        return new PageableRequest(
                page == null ? 0 : page,
                size == null ? 20 : size
        );
    }
}