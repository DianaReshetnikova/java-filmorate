package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

// Указываем, что класс является бином и его
// нужно добавить в контекст приложения
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserStorage userStorage;

    public Collection<User> getUsers() {
        return userStorage.getUsers();
    }

    public User createUser(User newUser) {
        return userStorage.createUser(newUser);
    }

    public User updateUser(User newUser) {
        return userStorage.updateUser(newUser);
    }

    public void deleteUser(Long id) {
        userStorage.deleteUser(id);
    }

    public User getUserById(Long id) {
        return userStorage.getUserById(id);
    }

    public void addFriend(Long user1Id, Long user2Id) {
        User user1 = userStorage.getUserById(user1Id);
        User user2 = userStorage.getUserById(user2Id);

        if (user1 != null && user2 != null) {
            Set<Long> user1Friends = user1.getFriendsIds();
            Set<Long> user2Friends = user2.getFriendsIds();
            if (!user1Friends.contains(user2Id) && !user2Friends.contains(user1Id)) {
                user1Friends.add(user2Id);
                user2Friends.add(user1Id);

                log.info("Пользователи {} и {} добавили друга друга в друзья.", user1Id, user2Id);
            }
        }
    }

    public void deleteFriend(Long user1Id, Long user2Id) {
        User user1 = userStorage.getUserById(user1Id);
        User user2 = userStorage.getUserById(user2Id);

        if (user1 != null && user2 != null) {
            Set<Long> user1Friends = user1.getFriendsIds();
            Set<Long> user2Friends = user2.getFriendsIds();

            if (user1Friends.contains(user2Id) && user2Friends.contains(user1Id)) {
                user1Friends.remove(user2Id);
                user2Friends.remove(user1Id);

                log.info("Пользователи {} и {} удалили друга друга из друзей.", user1Id, user2Id);
            }
        }
    }

    public Collection<User> getFriendsOfUser(Long id) {
        User user = userStorage.getUserById(id);

        Set<User> friends = user.getFriendsIds().stream()
                .map(userStorage::getUserById)
                .collect(Collectors.toSet());

        log.info("Пользователь {} имеет {} друзей.", id, friends.size());
        return friends;
    }

    public Collection<User> getIntersectingFriends(Long userId, Long friendId) {
        User user1 = userStorage.getUserById(userId);
        User user2 = userStorage.getUserById(friendId);

        Set<Long> user1Friends = user1.getFriendsIds();
        Set<Long> user2Friends = user2.getFriendsIds();

        //общие элементы Long для двух списков
        Set<Long> intersectingIds = user1Friends.stream()
                .filter(user2Friends::contains)
                .collect(Collectors.toSet());

        log.info("Пользователь {} и {} имеют {} общих друзей.", userId, friendId, intersectingIds.size());

        return intersectingIds.stream()
                .map(userStorage::getUserById)
                .collect(Collectors.toSet());

    }
}
