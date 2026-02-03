package com.nhnacademy.repository;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class UserRepository {
    private final Map<String, String> userDatabase = new HashMap<>();

    public UserRepository() {
        userDatabase.put("marco", "nhnacademy123");
        userDatabase.put("alice", "nhnacademy456");
        userDatabase.put("bob", "nhnacademy789");
    }

    public boolean authenticate(String userId, String password) {
        return userDatabase.containsKey(userId) && userDatabase.get(userId).equals(password);
    }

    public Set<String> getAllUserIds() {
        return Collections.unmodifiableSet(userDatabase.keySet());
    }
}
