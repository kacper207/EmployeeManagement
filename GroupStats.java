package com.example.demo2;

public class GroupStats {
    private String groupName;
    private Long groupId;
    private Long ratingCount;
    private Double averageRating;

    public GroupStats(String groupName, Long groupId, Long ratingCount, Double averageRating) {
        this.groupName = groupName;
        this.groupId = groupId;
        this.ratingCount = ratingCount;
        this.averageRating = averageRating != null ? averageRating : 0.0;
    }

    public String getGroupName() {
        return groupName;
    }

    public Long getGroupId() {
        return groupId;
    }

    public Long getRatingCount() {
        return ratingCount;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    @Override
    public String toString() {
        return String.format("%s (Ratings: %d, Avg: %.2f)", groupName, ratingCount, averageRating);
    }
}