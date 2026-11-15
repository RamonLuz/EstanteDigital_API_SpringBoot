package com.estantedigital.service;

import java.time.LocalDate;

public record CreateSaleCommand(
        String customerCpf,
        int employeeRegistrationNumber,
        String bookTitle,
        String bookAuthor,
        LocalDate saleDate) {
}
