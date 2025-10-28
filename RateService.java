package com.example.demo2;

import org.hibernate.Session;
import org.hibernate.Transaction;
import java.util.List;

public class RateService {
    public void saveRate(Rate rate) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(rate);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }

    public List<Rate> getRatesByGroup(EmployeeGroup group) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from Rate r where r.group.id = :groupId", Rate.class)
                    .setParameter("groupId", group.getId())
                    .list();
        }
    }

    public void deleteRate(Rate rate) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Rate managedRate = session.get(Rate.class, rate.getId());
            if (managedRate != null) {
                session.remove(managedRate);
                session.flush();
            }

            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            throw new RuntimeException("Failed to delete rating: " + e.getMessage(), e);
        }
    }

    public Double getAverageRatingForGroup(Long groupId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "select avg(r.value) from Rate r where r.group.id = :groupId", Double.class)
                    .setParameter("groupId", groupId)
                    .getSingleResult();
        }
    }
}