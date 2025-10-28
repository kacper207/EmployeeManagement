package com.example.demo2;

import java.util.HashMap;
import java.util.Map;

public class ClassContainer {
    private Map<String, classEmployee> groups;

    public ClassContainer() {
        this.groups = new HashMap<>();
    }

    public void addClass(String name, double capacity) {
        groups.put(name, new classEmployee(name, (int)capacity));
    }

    public void removeClass(String name) {
        groups.remove(name);
    }

    public Map<String, classEmployee> getGroups() {
        return groups;
    }

    public classEmployee getGroup(String name) {
        return groups.get(name);
    }
}