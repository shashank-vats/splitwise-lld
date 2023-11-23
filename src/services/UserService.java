package services;

import models.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class UserService {
    private final List<User> users;

    public UserService() {
        users = new ArrayList<>();
    }

    public void addUser(String userId, String name, String email, String phone) {
        User user = new User(userId, name, email, phone);
        users.add(user);
    }

    public User findUser(String userId) {
        for (User u : users) {
            if (u.getUserId().equals(userId)) return u;
        }
        return null;
    }
}
