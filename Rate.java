package com.example.demo2;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "rates")
public class Rate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private double value;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "group_id", nullable = false)
    private EmployeeGroup group;

    @Column(nullable = false)
    private LocalDate rateDate;

    @Column(nullable = true)
    private String comment;

    public Rate() {}

    public Rate(double value, EmployeeGroup group, LocalDate rateDate, String comment) {
        this.value = value;
        this.group = group;
        this.rateDate = rateDate;
        this.comment = comment;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public EmployeeGroup getGroup() {
        return group;
    }

    public void setGroup(EmployeeGroup group) {
        this.group = group;
    }

    public LocalDate getRateDate() {
        return rateDate;
    }

    public void setRateDate(LocalDate rateDate) {
        this.rateDate = rateDate;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}