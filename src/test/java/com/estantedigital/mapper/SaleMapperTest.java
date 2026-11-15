package com.estantedigital.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.estantedigital.dto.SaleRequestDTO;
import com.estantedigital.service.CreateSaleCommand;

class SaleMapperTest {

    @Test
    void shouldMapRequestDtoToApplicationCommand() {
        SaleRequestDTO dto = new SaleRequestDTO();
        dto.setCustomerCpf("12345678901");
        dto.setEmployeeRegistrationNumber(1001);
        dto.setBookTitle("Clean Code");
        dto.setBookAuthor("Robert Martin");
        dto.setSaleDate(LocalDate.of(2026, 10, 11));

        CreateSaleCommand command = SaleMapper.toCommand(dto);

        assertNotNull(command);
        assertEquals("12345678901", command.customerCpf());
        assertEquals(1001, command.employeeRegistrationNumber());
        assertEquals("Clean Code", command.bookTitle());
        assertEquals("Robert Martin", command.bookAuthor());
        assertEquals(LocalDate.of(2026, 10, 11), command.saleDate());
    }
}
