package com.healingsys.controllers;

import com.healingsys.dto.OperationDetailsDto;
import com.healingsys.dto.SimpleOperationDetailsDto;
import com.healingsys.services.OperationDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/operation")
public class OperationDetailsController {
    private final OperationDetailsService operationDetailsService;

    @GetMapping("/actives")
    public List<SimpleOperationDetailsDto> getActiveOperationDetails() {
        return operationDetailsService.getAllActive();
    }

    @GetMapping("/inactivates")
    public List<SimpleOperationDetailsDto> getInactiveOperationDetails() {
        return operationDetailsService.getAllInactive();
    }

    @GetMapping("/deleted")
    public List<SimpleOperationDetailsDto> getDeletedOperationDetails() {
        return operationDetailsService.getAllDeleted();
    }

    @GetMapping("/{id}")
    public OperationDetailsDto getOperationDetailsByName(@PathVariable Long id) {
        return operationDetailsService.getById(id);
    }

    @PostMapping("/add")
    public String addOperationDetails(@RequestBody OperationDetailsDto operationDetailsDto) {
        return operationDetailsService.saveOperationDetails(operationDetailsDto);
    }

    @PutMapping("/update/{id}")
    public String updateOperationDetails(
            @PathVariable Long id,
            @RequestBody OperationDetailsDto operationDetailsDto) {
        return operationDetailsService.updateOperationDetails(operationDetailsDto, id);
    }
}
