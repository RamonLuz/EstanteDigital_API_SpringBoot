package com.estantedigital.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.estantedigital.dto.SaleRequestDTO;
import com.estantedigital.dto.SaleResponseDTO;
import com.estantedigital.entity.Sale;
import com.estantedigital.mapper.SaleMapper;
import com.estantedigital.service.SaleService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/sales")
public class SaleController {

    private final SaleService saleService;

    public SaleController(SaleService saleService) {
        this.saleService = saleService;
    }

    @GetMapping
    public ResponseEntity<List<SaleResponseDTO>> getAll() {
        return ResponseEntity.ok(saleService.findAll().stream()
                .map(SaleMapper::toResponse)
                .toList());
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
        Sale sale = SaleMapper.toEntity(dto);
        Sale savedSale = saleService.save(sale);
        return ResponseEntity.status(HttpStatus.CREATED).body(SaleMapper.toResponse(savedSale));
    }
}
