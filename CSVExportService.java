package com.example.demo2;

import org.hibernate.Session;
import jakarta.persistence.criteria.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CSVExportService {

    public void exportEmployeesToCSV(String filePath) {
        try (Session session = HibernateUtil.getSessionFactory().openSession();
             FileWriter writer = new FileWriter(filePath)) {
            List<Employee> employees = session.createQuery(
                    "from Employee e join fetch e.group", Employee.class).list();
            writer.write("ID,First Name,Last Name,Status,Birth Year,Salary,Group Name\n");
            for (Employee employee : employees) {
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void exportGroupStatsToCSV(String filePath) {
        try (Session session = HibernateUtil.getSessionFactory().openSession();
             FileWriter writer = new FileWriter(filePath)) {
            writer.write("Group Name,Employee Count,Rating Count,Average Rating\n");
            List<GroupStats> groupStatsList = getGroupStatsByCriteria();
            for (GroupStats stats : groupStatsList) {
                Long employeeCount = session.createQuery(
                                "select count(e) from Employee e where e.group.id = :groupId", Long.class)
                        .setParameter("groupId", stats.getGroupId())
                        .getSingleResult();
                writer.write(String.format("%s,%d,%d,%.2f\n",
                        stats.getGroupName(),
                        employeeCount,
                        stats.getRatingCount(),
                        stats.getAverageRating()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<GroupStats> getGroupStatsByCriteria() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<GroupStats> criteriaQuery = builder.createQuery(GroupStats.class);
            Root<EmployeeGroup> root = criteriaQuery.from(EmployeeGroup.class);
            Join<EmployeeGroup, Rate> rateJoin = root.join("rates", JoinType.LEFT);
            criteriaQuery.groupBy(root.get("name"), root.get("id"));
            criteriaQuery.select(builder.construct(
                    GroupStats.class,
                    root.get("name"),
                    root.get("id"),
                    builder.count(rateJoin.get("id")),
                    builder.coalesce(builder.avg(rateJoin.get("value")), 0.0)
            ));
            return session.createQuery(criteriaQuery).getResultList();
        }
    }
}