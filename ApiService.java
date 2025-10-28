package com.example.demo2;

import org.springframework.stereotype.Service;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import java.io.StringWriter;
import java.util.List;

@Service
public class ApiService {

    private final DatabaseService databaseService;
    private final RateService rateService;
    private final GroupContainer groupContainer;

    public ApiService() {
        this.databaseService = new DatabaseService();
        this.rateService = new RateService();
        this.groupContainer = new GroupContainer();
        loadGroups();
    }

    private void loadGroups() {
        List<EmployeeGroup> groups = databaseService.getAllGroupsWithEmployees();
        for (EmployeeGroup group : groups) {
            groupContainer.addClass(group);
        }
    }

    public Employee addEmployee(Employee employee) {
        if (employee.getGroup() != null) {
            EmployeeGroup group = findGroupById(employee.getGroup().getId());
            if (group == null) {
                throw new IllegalArgumentException("Group not found");
            }
            if (group.getEmployees().size() >= group.getMaxSize()) {
                throw new IllegalArgumentException("Group is full");
            }
            employee.setGroup(group);
        }
        databaseService.saveEmployee(employee);
        return employee;
    }

    public void deleteEmployee(Long id) {
        Employee employee = findEmployeeById(id);
        if (employee == null) {
            throw new IllegalArgumentException("Employee not found");
        }
        databaseService.deleteEmployee(employee);
    }

    public ResponseEntity<String> getEmployeesCSV() {
        try {
            StringWriter writer = new StringWriter();
            List<EmployeeGroup> groups = databaseService.getAllGroupsWithEmployees();

            writer.write("ID,First Name,Last Name,Status,Birth Year,Salary,Group Name\n");

            for (EmployeeGroup group : groups) {
                for (Employee employee : group.getEmployees()) {
                    String groupName = employee.getGroup() != null ? employee.getGroup().getName() : "N/A";
                    writer.write(String.format("%d,%s,%s,%s,%d,%.2f,%s\n",
                            employee.getId(),
                            employee.getFirstName(),
                            employee.getLastName(),
                            employee.getCondition(),
                            employee.getBirthYear(),
                            employee.getSalary(),
                            groupName));
                }
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv"));
            headers.setContentDispositionFormData("attachment", "employees.csv");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(writer.toString());

        } catch (Exception e) {
            throw new RuntimeException("Error generating CSV", e);
        }
    }

    public List<EmployeeGroup> getAllGroups() {
        return databaseService.getAllGroupsWithEmployees();
    }

    public EmployeeGroup addGroup(EmployeeGroup group) {
        if (groupContainer.getGroup(group.getName()) != null) {
            throw new IllegalArgumentException("Group with this name already exists");
        }
        databaseService.saveGroup(group);
        groupContainer.addClass(group);
        return group;
    }

    public void deleteGroup(Long id) {
        EmployeeGroup group = findGroupById(id);
        if (group == null) {
            throw new IllegalArgumentException("Group not found");
        }
        databaseService.deleteGroup(group);
        groupContainer.removeClass(group.getName());
    }

    public List<Employee> getEmployeesByGroup(Long groupId) {
        EmployeeGroup group = findGroupById(groupId);
        if (group == null) {
            throw new IllegalArgumentException("Group not found");
        }
        return group.getEmployees();
    }

    public GroupFillResponse getGroupFill(Long groupId) {
        EmployeeGroup group = findGroupById(groupId);
        if (group == null) {
            throw new IllegalArgumentException("Group not found");
        }

        int currentSize = group.getEmployees().size();
        int maxSize = group.getMaxSize();
        double fillPercentage = maxSize > 0 ? (double) currentSize / maxSize * 100 : 0.0;

        return new GroupFillResponse(group.getName(), currentSize, maxSize, fillPercentage);
    }

    public Rate addRating(Rate rate) {
        if (rate.getGroup() == null || rate.getGroup().getId() == null) {
            throw new IllegalArgumentException("Group ID is required");
        }

        EmployeeGroup group = findGroupById(rate.getGroup().getId());
        if (group == null) {
            throw new IllegalArgumentException("Group not found");
        }

        rate.setGroup(group);
        rateService.saveRate(rate);
        return rate;
    }

    private EmployeeGroup findGroupById(Long id) {
        List<EmployeeGroup> groups = databaseService.getAllGroupsWithEmployees();
        return groups.stream()
                .filter(g -> g.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    private Employee findEmployeeById(Long id) {
        List<EmployeeGroup> groups = databaseService.getAllGroupsWithEmployees();
        for (EmployeeGroup group : groups) {
            for (Employee employee : group.getEmployees()) {
                if (employee.getId().equals(id)) {
                    return employee;
                }
            }
        }
        return null;
    }

    public static class GroupFillResponse {
        private String groupName;
        private int currentSize;
        private int maxSize;
        private double fillPercentage;

        public GroupFillResponse(String groupName, int currentSize, int maxSize, double fillPercentage) {
            this.groupName = groupName;
            this.currentSize = currentSize;
            this.maxSize = maxSize;
            this.fillPercentage = fillPercentage;
        }

        public String getGroupName() { return groupName; }
        public int getCurrentSize() { return currentSize; }
        public int getMaxSize() { return maxSize; }
        public double getFillPercentage() { return fillPercentage; }
    }
}