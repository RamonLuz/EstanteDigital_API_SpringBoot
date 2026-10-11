package com.estantedigital.mapper;

import java.util.List;

import com.estantedigital.dto.CustomerRequestDTO;
import com.estantedigital.dto.CustomerResponseDTO;
import com.estantedigital.entity.Customer;

public final class CustomerMapper {

    private CustomerMapper() {
    }

    public static Customer toEntity(CustomerRequestDTO dto) {
        Customer customer = new Customer();
        customer.setName(dto.getName());
        customer.setCpf(dto.getCpf());
        customer.setStatus(dto.getStatus());
        customer.setPurchases(dto.getPurchases());
        return customer;
    }

    public static CustomerResponseDTO toResponse(Customer entity) {
        if (entity == null) {
            return null;
        }

        List<com.estantedigital.dto.BookResponseDTO> purchases = entity.getPurchases() == null ? null
                : entity.getPurchases().stream()
                        .map(BookMapper::toResponse)
                        .toList();

        return new CustomerResponseDTO(
                entity.getId(),
                entity.getName(),
                entity.getCpf(),
                entity.getStatus(),
                purchases);
    }

    public static void applyUpdate(Customer entity, CustomerRequestDTO dto) {
        if (dto.getStatus() != null) {
            entity.setStatus(dto.getStatus());
        }
    }
}
