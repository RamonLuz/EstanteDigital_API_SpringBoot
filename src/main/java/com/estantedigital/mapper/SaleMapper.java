package com.estantedigital.mapper;

import java.time.LocalDate;

import com.estantedigital.dto.SaleRequestDTO;
import com.estantedigital.dto.SaleResponseDTO;
import com.estantedigital.entity.Book;
import com.estantedigital.entity.Customer;
import com.estantedigital.entity.Employee;
import com.estantedigital.entity.Sale;

public final class SaleMapper {

    private SaleMapper() {
    }

    public static Sale toEntity(SaleRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        Customer customer = new Customer();
        customer.setCpf(dto.getCustomerCpf());

        Employee employee = new Employee();
        employee.setRegistrationNumber(dto.getEmployeeRegistrationNumber());

        Book book = new Book();
        book.setTitle(dto.getBookTitle());
        book.setAuthor(dto.getBookAuthor());

        Sale sale = new Sale();
        sale.setCustomer(customer);
        sale.setEmployee(employee);
        sale.setBook(book);
        sale.setSaleDate(dto.getSaleDate() == null ? LocalDate.now() : dto.getSaleDate());
        return sale;
    }

    public static SaleResponseDTO toResponse(Sale entity) {
        if (entity == null) {
            return null;
        }

        return new SaleResponseDTO(
                entity.getId(),
                entity.getCustomer() == null ? null : CustomerMapper.toResponse(entity.getCustomer()),
                entity.getEmployee() == null ? null : EmployeeMapper.toResponse(entity.getEmployee()),
                entity.getBook() == null ? null : BookMapper.toResponse(entity.getBook()),
                entity.getSaleDate());
    }
}
