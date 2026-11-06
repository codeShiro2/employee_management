
package com.example.employee.controller;

import com.example.employee.model.Employee;
import com.example.employee.repository.EmployeeRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.net.URI;
import java.util.Optional;

@Controller
@RequestMapping("/employees")
public class EmployeeController {

    private final EmployeeRepository employeeRepository;

    public EmployeeController(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    // CREATE -> POST /employees
    @PostMapping
    @ResponseBody
    public ResponseEntity<?> createEmployee(Employee employee) {

        if (employee.getEmployeeId() == null || employee.getEmployeeId().isBlank()) {
            return ResponseEntity.badRequest().body("Employee ID is required");
        }

        if (employeeRepository.existsById(employee.getEmployeeId())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Employee with ID " + employee.getEmployeeId() + " already exists");
        }

        Employee saved = employeeRepository.save(employee);

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // READ ALL -> GET /employees/displayAll
    // Displays employees in a Thymeleaf table
    @GetMapping("/displayAll")
    public String displayAll(Model model) {

        model.addAttribute("employees", employeeRepository.findAll());

        return "displayAll";
    }

    // READ ONE -> GET /employees/display/{employeeId}
    @GetMapping("/display/{employeeId}")
    @ResponseBody
    public ResponseEntity<?> displayById(@PathVariable String employeeId) {

        Optional<Employee> employee = employeeRepository.findById(employeeId);

        if (employee.isPresent()) {
            return ResponseEntity.ok(employee.get());
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Employee with ID " + employeeId + " not found");
    }

    // UPDATE helper -> PUT /employees/update
    @PutMapping("/update")
    @ResponseBody
    public ResponseEntity<?> updateFromForm(Employee employee) {

        return updateEmployee(employee.getEmployeeId(), employee);
    }

    // UPDATE -> PUT /employees/update/{employeeId}
    @PutMapping("/update/{employeeId}")
    @ResponseBody
    public ResponseEntity<?> updateEmployee(
            @PathVariable String employeeId, Employee updated) {

        Optional<Employee> existing = employeeRepository.findById(employeeId);

        if (existing.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Employee with ID " + employeeId + " not found");
        }

        Employee employee = existing.get();

        employee.setEmployeeName(updated.getEmployeeName());
        employee.setEmployeeEmail(updated.getEmployeeEmail());
        employee.setLocation(updated.getLocation());

        return ResponseEntity.ok(employeeRepository.save(employee));
    }

    // DELETE -> DELETE /employees/delete/{employeeId}
    @DeleteMapping("/delete/{employeeId}")
    @ResponseBody
    public ResponseEntity<String> deleteEmployee(
            @PathVariable String employeeId) {

        if (!employeeRepository.existsById(employeeId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Employee with ID " + employeeId + " not found");
        }

        employeeRepository.deleteById(employeeId);

        return ResponseEntity.ok(
                "Employee with ID " + employeeId + " deleted successfully");
    }

    // SEARCH helper -> GET /employees/search?employeeId=E101
    @GetMapping("/search")
    @ResponseBody
    public ResponseEntity<Void> search(@RequestParam String employeeId) {

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("/employees/display/" + employeeId))
                .build();
    }

    // DELETE helper -> DELETE /employees/delete?employeeId=E101
    @DeleteMapping("/delete")
    @ResponseBody
    public ResponseEntity<String> deleteFromForm(
            @RequestParam String employeeId) {

        return deleteEmployee(employeeId);
    }
}
