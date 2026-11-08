package com.estantedigital.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

import com.estantedigital.exception.ApiException;

class PaginationTest {

    @Test
    void shouldCreatePageRequestSortedById() {
        Pageable pageable = Pagination.request(2, 20);

        assertEquals(2, pageable.getPageNumber());
        assertEquals(20, pageable.getPageSize());
        assertEquals("id: ASC", pageable.getSort().toString());
    }

    @Test
    void shouldRejectNegativePage() {
        ApiException exception = assertThrows(ApiException.class, () -> Pagination.request(-1, 10));

        assertEquals(400, exception.getStatus());
        assertEquals("page", exception.getField());
        assertEquals("Page must be zero or greater", exception.getMessage());
    }

    @Test
    void shouldRejectSizeOutsideAllowedRange() {
        ApiException tooSmall = assertThrows(ApiException.class, () -> Pagination.request(0, 0));
        ApiException tooLarge = assertThrows(ApiException.class, () -> Pagination.request(0, 101));

        assertEquals("size", tooSmall.getField());
        assertEquals("Size must be between 1 and 100", tooSmall.getMessage());
        assertEquals("size", tooLarge.getField());
        assertEquals("Size must be between 1 and 100", tooLarge.getMessage());
    }
}
