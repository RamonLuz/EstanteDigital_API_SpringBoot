package com.estantedigital.mapper;

import com.estantedigital.dto.EmployeeRequestDTO;
import com.estantedigital.dto.EmployeeResponseDTO;
import com.estantedigital.entity.Employee;

public final class EmployeeMapper {

    private EmployeeMapper() {
    }

    public static Employee toEntity(EmployeeRequestDTO dto) {
        Employee employee = new Employee();
        employee.setName(dto.getName());
        employee.setCpf(dto.getCpf());
        employee.setRegistrationNumber(dto.getRegistrationNumber());
        employee.setPosition(dto.getPosition());
        employee.setStatus(dto.getStatus());
        return employee;
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

    public static void applyUpdate(Employee entity, EmployeeRequestDTO dto) {
        if (dto.getStatus() != null) {
            entity.setStatus(dto.getStatus());
        }
    }
}
