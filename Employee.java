package com.example.demo2;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

enum EmployeeCondition {
    obecny, delegacja, chory, nieobecny
}

@Entity
@Table(name = "employees")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Enumerated(EnumType.STRING)
    @Column(name="emp_condition")
    private EmployeeCondition condition;


    @Column(name = "birth_year", nullable = false)
    private int birthYear;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    private double salary;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "group_id")
    @JsonBackReference
    private EmployeeGroup group;

    public Employee() {}

    public Employee(String firstName, String lastName, EmployeeCondition condition, int birthYear, double salary) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.condition = condition;
        this.birthYear = birthYear;
        this.salary = salary;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public EmployeeCondition getCondition() { return condition; }
    public void setCondition(EmployeeCondition condition) { this.condition = condition; }

    public int getBirthYear() { return birthYear; }
    public void setBirthYear(int birthYear) { this.birthYear = birthYear; }

    public double getSalary() { return salary; }
    public void setSalary(double salary) { this.salary = salary; }

    public EmployeeGroup getGroup() { return group; }
    public void setGroup(EmployeeGroup group) { this.group = group; }

    @Override
    public String toString() {
        return firstName + " " + lastName + ", " + condition + ", " + birthYear + ", " + salary;
    }
}