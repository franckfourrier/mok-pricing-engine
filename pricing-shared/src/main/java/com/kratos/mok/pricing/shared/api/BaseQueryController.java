package com.kratos.mok.pricing.shared.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public abstract class BaseQueryController {

    protected int normalizePage(Integer page) {
        return page == null ? 0 : Math.max(page, 0);
    }

    protected int normalizeSize(Integer size) {
        return size == null ? 20 : Math.min(Math.max(size, 1), 100);
    }

    protected <T extends Enum<T>> T parseEnumSafe(
            String value,
            Class<T> enumClass,
            String fieldName
    ) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return Enum.valueOf(enumClass, value.trim().toUpperCase());
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    fieldName + " invalid value: " + value
            );
        }
    }
}