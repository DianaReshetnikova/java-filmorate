package ru.yandex.practicum.filmorate.service.interfaces;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserService {
    Collection<User> getUsers();

    User createUser(User newUser);

    User updateUser(User newUser);

    void deleteUser(Long id);

    User getUserById(Long id);

    void addFriend(Long userId, Long friendId);

    void deleteFriend(Long userId, Long friendId);

    Collection<User> getFriendsOfUser(Long id);

    Collection<User> getIntersectingFriends(Long userId, Long friendId);
}
