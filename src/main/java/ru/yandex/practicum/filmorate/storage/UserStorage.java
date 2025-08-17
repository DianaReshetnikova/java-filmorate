package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage {
    public Collection<User> getUsers();

    public User createUser(User newUser);

    public User updateUser(User newUser);

    public void deleteUser(Long id);

    public User getUserById(Long id);
}
