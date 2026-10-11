package com.estantedigital.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.estantedigital.dto.SaleRequestDTO;
import com.estantedigital.entity.Sale;

class SaleMapperTest {

    @Test
    void shouldMapRequestDtoToEntity() {
        SaleRequestDTO dto = new SaleRequestDTO();
        dto.setCustomerCpf("12345678901");
        dto.setEmployeeRegistrationNumber(1001);
        dto.setBookTitle("Clean Code");
        dto.setBookAuthor("Robert Martin");
        dto.setSaleDate(LocalDate.of(2026, 10, 11));

        Sale sale = SaleMapper.toEntity(dto);

        assertNotNull(sale);
        assertEquals("12345678901", sale.getCustomer().getCpf());
        assertEquals(1001, sale.getEmployee().getRegistrationNumber());
        assertEquals("Clean Code", sale.getBook().getTitle());
        assertEquals("Robert Martin", sale.getBook().getAuthor());
        assertEquals(LocalDate.of(2026, 10, 11), sale.getSaleDate());
    }
}
