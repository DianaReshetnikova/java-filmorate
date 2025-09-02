package ru.yandex.practicum.filmorate.storage.interfaces;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Optional;

public interface UserStorage {
    public Collection<User> getUsers();

    public User createUser(User newUser);

    public User updateUser(User newUser);

    public void deleteUser(Long id);

    public Optional<User> getUserById(Long id);

    public void addFriend(Long userId, Long friendId);

    public void removeFriend(Long userId, Long friendId);

    public Collection<User> getFriendsOfUser(Long id);

    public Collection<User> getIntersectingFriends(Long userId, Long friendId);

    void deleteAll();
}
