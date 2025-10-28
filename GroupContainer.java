package com.example.demo2;

import java.util.HashMap;
import java.util.Map;

public class GroupContainer {
    private Map<String, EmployeeGroup> groups;

    public GroupContainer() {
        this.groups = new HashMap<>();
    }

    public void addClass(String name, double capacity) {
        EmployeeGroup group = new EmployeeGroup(name, (int)capacity);
        groups.put(name, group);
    }

    public void addClass(EmployeeGroup group) {
        groups.put(group.getName(), group);
    }

    public void removeClass(String name) {
        groups.remove(name);
    }

    public Map<String, EmployeeGroup> getGroups() {
        return groups;
    }

    public EmployeeGroup getGroup(String name) {
        return groups.get(name);
    }
}