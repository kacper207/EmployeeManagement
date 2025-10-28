package com.example.demo2;

import java.util.ArrayList;
import java.util.List;

class classEmployee {
    private String name;
    private int maxSize;
    private List<Employee> employees;

    public classEmployee(String name, int maxSize) {
        this.name = name;
        this.maxSize = maxSize;
        this.employees = new ArrayList<>();
    }

    public void addEmployee(Employee employee) {
        if (employees.size() < maxSize) {
            employees.add(employee);
        } else {
            System.out.println("Cannot add more employees. Maximum size reached.");
        }
    }

    public void removeEmployee(Employee employee) {
        employees.remove(employee);
    }

    public List<Employee> getEmployees() {
        return employees;
    }

    public String getName() {
        return name;
    }

    public int getMaxSize() {
        return maxSize;
    }
}