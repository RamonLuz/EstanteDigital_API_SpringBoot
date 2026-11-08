package com.estantedigital.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.estantedigital.exception.ApiException;

public final class Pagination {

    private static final int MAX_PAGE_SIZE = 100;

    private Pagination() {
    }

    public static Pageable request(int page, int size) {
        if (page < 0) {
            throw new ApiException(400, "Page must be zero or greater", "page");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new ApiException(400, "Size must be between 1 and " + MAX_PAGE_SIZE, "size");
        }

        return PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id"));
    }
}
