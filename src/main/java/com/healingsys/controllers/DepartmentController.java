package com.healingsys.controllers;

import com.healingsys.dto.department.DepartmentDto;
import com.healingsys.dto.department.SimpleDepartmentDto;
import com.healingsys.exceptions.ApiAlreadyExistException;
import com.healingsys.exceptions.ApiIllegalArgumentException;
import com.healingsys.exceptions.ApiNoContentException;
import com.healingsys.exceptions.ApiNoSuchElementException;
import com.healingsys.services.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/department")
public class DepartmentController {
    private final DepartmentService departmentService;


    @GetMapping("/actives")
    public List<SimpleDepartmentDto> getActiveDepartmentDetails()
            throws ApiNoSuchElementException {

        return departmentService.getAllActive();
    }


    @GetMapping("/inactivates")
    public List<SimpleDepartmentDto> getInactiveDepartmentDetails()
            throws ApiNoSuchElementException {

        return departmentService.getAllInactive();
    }


    @GetMapping("/deleted")
    public List<SimpleDepartmentDto> getDeletedDepartmentDetails()
            throws ApiNoSuchElementException {

        return departmentService.getAllDeleted();
    }


    @GetMapping("/{id}")
    public DepartmentDto getDepartmentDetailsById(@PathVariable Long id)
            throws ApiNoSuchElementException {

        return departmentService.getDepartmentDtoById(id);
    }


    @PostMapping("/add")
    public ResponseEntity<String> addDepartmentDetails(@RequestBody DepartmentDto departmentDto)
            throws ApiAlreadyExistException, ApiIllegalArgumentException, ApiNoContentException {

        return new ResponseEntity<>(
                departmentService.saveDepartmentDetails(departmentDto),
                HttpStatus.CREATED);
    }


    @PutMapping("/update/{id}")
    public ResponseEntity<String> updateDepartmentDetails(
            @PathVariable Long id,
            @RequestBody DepartmentDto departmentDto)
            throws ApiAlreadyExistException, ApiIllegalArgumentException, ApiNoContentException {

        return new ResponseEntity<>(
                departmentService.updateDepartmentDetails(departmentDto, id),
                HttpStatus.ACCEPTED);
    }
}
