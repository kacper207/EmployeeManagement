package com.example.demo2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
public class EmployeeRestController {

    @Autowired
    private ApiService apiService;

    @PostMapping("/employee")
    public ResponseEntity<?> addEmployee(@RequestBody Employee employee) {
        try {
            if (employee.getFirstName() == null || employee.getFirstName().trim().isEmpty() ||
                    employee.getLastName() == null || employee.getLastName().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ErrorResponse("First name and last name are required"));
            }

            if (employee.getBirthYear() <= 1900 || employee.getBirthYear() > java.time.Year.now().getValue()) {
                return ResponseEntity.badRequest()
                        .body(new ErrorResponse("Invalid birth year"));
            }

            if (employee.getSalary() < 0) {
                return ResponseEntity.badRequest()
                        .body(new ErrorResponse("Salary cannot be negative"));
            }

            Employee savedEmployee = apiService.addEmployee(employee);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedEmployee);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Internal server error"));
        }
    }

    @DeleteMapping("/employee/{id}")
    public ResponseEntity<?> deleteEmployee(@PathVariable Long id) {
        try {
            apiService.deleteEmployee(id);
            return ResponseEntity.noContent().build();

        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Internal server error"));
        }
    }

    @GetMapping("/employee/csv")
    public ResponseEntity<String> getEmployeesCSV() {
        try {
            return apiService.getEmployeesCSV();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error generating CSV: " + e.getMessage());
        }
    }

    @GetMapping("/group")
    public ResponseEntity<?> getAllGroups() {
        try {
            List<EmployeeGroup> groups = apiService.getAllGroups();
            return ResponseEntity.ok(groups);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Internal server error"));
        }
    }

    @PostMapping("/group")
    public ResponseEntity<?> addGroup(@RequestBody EmployeeGroup group) {
        try {
            if (group.getName() == null || group.getName().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ErrorResponse("Group name is required"));
            }

            if (group.getMaxSize() <= 0) {
                return ResponseEntity.badRequest()
                        .body(new ErrorResponse("Max size must be positive"));
            }

            EmployeeGroup savedGroup = apiService.addGroup(group);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedGroup);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Internal server error"));
        }
    }

    @DeleteMapping("/group/{id}")
    public ResponseEntity<?> deleteGroup(@PathVariable Long id) {
        try {
            apiService.deleteGroup(id);
            return ResponseEntity.noContent().build();

        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Internal server error"));
        }
    }

    @GetMapping("/group/{id}/employee")
    public ResponseEntity<?> getEmployeesByGroup(@PathVariable Long id) {
        try {
            List<Employee> employees = apiService.getEmployeesByGroup(id);
            return ResponseEntity.ok(employees);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Internal server error"));
        }
    }

    @GetMapping("/group/{id}/fill")
    public ResponseEntity<?> getGroupFill(@PathVariable Long id) {
        try {
            ApiService.GroupFillResponse fillResponse = apiService.getGroupFill(id);
            return ResponseEntity.ok(fillResponse);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Internal server error"));
        }
    }

    @PostMapping("/rating")
    public ResponseEntity<?> addRating(@RequestBody Rate rate) {
        try {
            if (rate.getValue() < 0 || rate.getValue() > 6) {
                return ResponseEntity.badRequest()
                        .body(new ErrorResponse("Rating value must be between 0 and 6"));
            }

            if (rate.getRateDate() == null) {
                return ResponseEntity.badRequest()
                        .body(new ErrorResponse("Rate date is required"));
            }

            Rate savedRate = apiService.addRating(rate);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedRate);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Internal server error"));
        }
    }

    public static class ErrorResponse {
        private String error;

        public ErrorResponse(String error) {
            this.error = error;
        }

        public String getError() {
            return error;
        }
    }
}