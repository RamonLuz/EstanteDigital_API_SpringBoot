package com.estantedigital.controller;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.estantedigital.dto.SaleRequestDTO;
import com.estantedigital.dto.SaleResponseDTO;
import com.estantedigital.dto.PageResponseDTO;
import com.estantedigital.entity.Sale;
import com.estantedigital.mapper.SaleMapper;
import com.estantedigital.service.SaleService;
import com.estantedigital.util.Pagination;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/sales")
public class SaleController {

    private final SaleService saleService;

    public SaleController(SaleService saleService) {
        this.saleService = saleService;
    }

    @GetMapping
    public ResponseEntity<PageResponseDTO<SaleResponseDTO>> getAll(
            @RequestParam(required = false) String customerCpf,
            @RequestParam(required = false) String bookTitle,
            @RequestParam(required = false) Integer employeeRegistrationNumber,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = Pagination.request(page, size);
        Page<SaleResponseDTO> result = saleService.search(
                customerCpf, bookTitle, employeeRegistrationNumber, pageable).map(SaleMapper::toResponse);
        return ResponseEntity.ok(PageResponseDTO.from(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SaleResponseDTO> getById(@PathVariable Long id) {
        Optional<Sale> sale = saleService.findById(id);

        if (sale.isPresent()) {
            return ResponseEntity.ok(SaleMapper.toResponse(sale.get()));
        }

        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<SaleResponseDTO> create(@Valid @RequestBody SaleRequestDTO dto) {
        Sale savedSale = saleService.save(SaleMapper.toCommand(dto));
        return ResponseEntity.status(HttpStatus.CREATED).body(SaleMapper.toResponse(savedSale));
    }
}
