package com.example.demo2;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "employee_groups")
public class EmployeeGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(name = "max_size", nullable = false)
    private int maxSize;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<Employee> employees = new ArrayList<>();

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Rate> rates = new ArrayList<>();

    public EmployeeGroup() {}

    public EmployeeGroup(String name, int maxSize) {
        this.name = name;
        this.maxSize = maxSize;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getMaxSize() { return maxSize; }
    public void setMaxSize(int maxSize) { this.maxSize = maxSize; }

    public List<Rate> getRates() { return rates; }
    public void setRates(List<Rate> rates) { this.rates = rates; }

    public void addEmployee(Employee employee) {
        if (employees.size() < maxSize) {
            employees.add(employee);
            employee.setGroup(this);
        } else {
            System.out.println("Cannot add more employees. Maximum size reached.");
        }
    }

    public void removeEmployee(Employee employee) {
        if (employees.remove(employee)) {
            employee.setGroup(null);
        }
    }

    public List<Employee> getEmployees() {
        return employees;
    }
}