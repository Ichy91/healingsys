package com.healingsys.controllers;

import com.healingsys.dto.DepartmentDetailsDto;
import com.healingsys.dto.SimpleDepartmentDetailsDto;
import com.healingsys.services.DepartmentDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/operation")
public class DepartmentDetailsController {
    private final DepartmentDetailsService departmentDetailsService;

    @GetMapping("/actives")
    public List<SimpleDepartmentDetailsDto> getActiveDepartmentDetails() throws NoSuchElementException {
        return departmentDetailsService.getAllActive();
    }

    @GetMapping("/inactivates")
    public List<SimpleDepartmentDetailsDto> getInactiveDepartmentDetails() throws NoSuchElementException {
        return departmentDetailsService.getAllInactive();
    }

    @GetMapping("/deleted")
    public List<SimpleDepartmentDetailsDto> getDeletedDepartmentDetails() throws NoSuchElementException {
        return departmentDetailsService.getAllDeleted();
    }

    @GetMapping("/{id}")
    public DepartmentDetailsDto getDepartmentDetailsByName(@PathVariable Long id) throws NoSuchElementException {
        return departmentDetailsService.getById(id);
    }

    @PostMapping("/add")
    public String addDepartmentDetails(@RequestBody DepartmentDetailsDto departmentDetailsDto) throws IllegalAccessException {
        return departmentDetailsService.saveDepartmentDetails(departmentDetailsDto);
    }

    @PutMapping("/update/{id}")
    public String updateDepartmentDetails(
            @PathVariable Long id,
            @RequestBody DepartmentDetailsDto departmentDetailsDto) throws NoSuchElementException {
        return departmentDetailsService.updateDepartmentDetails(departmentDetailsDto, id);
    }
}
