package com.estantedigital.mapper;

import com.estantedigital.dto.EmployeeRequestDTO;
import com.estantedigital.dto.EmployeeResponseDTO;
import com.estantedigital.entity.Employee;

public final class EmployeeMapper {

    private EmployeeMapper() {
    }

    public static Employee toEntity(EmployeeRequestDTO dto) {
        return new Employee(
                dto.getName(),
                dto.getCpf(),
                dto.getRegistrationNumber(),
                dto.getPosition(),
                dto.getStatus());
    }

    public static EmployeeResponseDTO toResponse(Employee entity) {
        if (entity == null) {
            return null;
        }

        return new EmployeeResponseDTO(
                entity.getId(),
                entity.getName(),
                entity.getCpf(),
                entity.getRegistrationNumber(),
                entity.getPosition(),
                entity.getStatus());
    }

}
