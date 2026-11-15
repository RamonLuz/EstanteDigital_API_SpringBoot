package com.estantedigital.mapper;

import com.estantedigital.dto.SaleRequestDTO;
import com.estantedigital.dto.SaleResponseDTO;
import com.estantedigital.entity.Sale;
import com.estantedigital.service.CreateSaleCommand;

public final class SaleMapper {

    private SaleMapper() {
    }

    public static CreateSaleCommand toCommand(SaleRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        return new CreateSaleCommand(
                dto.getCustomerCpf(),
                dto.getEmployeeRegistrationNumber(),
                dto.getBookTitle(),
                dto.getBookAuthor(),
                dto.getSaleDate());
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
