package com.healingsys.controllers;

import com.healingsys.dto.department.DepartmentDetailsDto;
import com.healingsys.dto.department.SimpleDepartmentDetailsDto;
import com.healingsys.exceptions.ApiAlreadyExistException;
import com.healingsys.exceptions.ApiIllegalArgumentException;
import com.healingsys.exceptions.ApiNoContentException;
import com.healingsys.exceptions.ApiNoSuchElementException;
import com.healingsys.services.DepartmentDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/department")
public class DepartmentDetailsController {
    private final DepartmentDetailsService departmentDetailsService;


    @GetMapping("/actives")
    public List<SimpleDepartmentDetailsDto> getActiveDepartmentDetails()
            throws ApiNoSuchElementException {

        return departmentDetailsService.getAllActive();
    }


    @GetMapping("/inactivates")
    public List<SimpleDepartmentDetailsDto> getInactiveDepartmentDetails()
            throws ApiNoSuchElementException {

        return departmentDetailsService.getAllInactive();
    }


    @GetMapping("/deleted")
    public List<SimpleDepartmentDetailsDto> getDeletedDepartmentDetails()
            throws ApiNoSuchElementException {

        return departmentDetailsService.getAllDeleted();
    }


    @GetMapping("/{id}")
    public DepartmentDetailsDto getDepartmentDetailsById(@PathVariable Long id)
            throws ApiNoSuchElementException {

        return departmentDetailsService.getDepartmentDtoById(id);
    }


    @PostMapping("/add")
    public ResponseEntity<String> addDepartmentDetails(@RequestBody DepartmentDetailsDto departmentDetailsDto)
            throws ApiAlreadyExistException, ApiIllegalArgumentException, ApiNoContentException {

        return new ResponseEntity<>(
                departmentDetailsService.saveDepartmentDetails(departmentDetailsDto),
                HttpStatus.CREATED);
    }


    @PutMapping("/update/{id}")
    public ResponseEntity<String> updateDepartmentDetails(
            @PathVariable Long id,
            @RequestBody DepartmentDetailsDto departmentDetailsDto)
            throws ApiAlreadyExistException, ApiIllegalArgumentException, ApiNoContentException {

        return new ResponseEntity<>(
                departmentDetailsService.updateDepartmentDetails(departmentDetailsDto, id),
                HttpStatus.ACCEPTED);
    }
}
