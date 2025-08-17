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

    public void addFriend(Long user1_Id, Long user2_Id) {
        User user1 = userStorage.getUserById(user1_Id);
        User user2 = userStorage.getUserById(user2_Id);

        if (user1 != null && user2 != null) {
            Set<Long> user1_Friends = user1.getFriendsIds();
            user1_Friends.add(user2_Id);
            user1.setFriendsIds(user1_Friends);

            Set<Long> user2_Friends = user2.getFriendsIds();
            user2_Friends.add(user1_Id);
            user2.setFriendsIds(user2_Friends);

            log.info("Пользователи {} и {} добавили друга друга в друзья.", user1_Id, user2_Id);
        }
    }

    public void deleteFriend(Long user1_Id, Long user2_Id) {
        User user1 = userStorage.getUserById(user1_Id);
        User user2 = userStorage.getUserById(user2_Id);

        if (user1 != null && user2 != null) {
            Set<Long> user1_Friends = user1.getFriendsIds();
            user1_Friends.remove(user2_Id);
            user1.setFriendsIds(user1_Friends);

            Set<Long> user2_Friends = user2.getFriendsIds();
            user2_Friends.remove(user1_Id);
            user2.setFriendsIds(user2_Friends);

            log.info("Пользователи {} и {} удалили друга друга из друзей.", user1_Id, user2_Id);
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

        Set<Long> user1_Friends = user1.getFriendsIds();
        Set<Long> user2_Friends = user2.getFriendsIds();

        //общие элементы Long для двух списков
        Set<Long> intersectingIds = user1_Friends.stream()
                .filter(user2_Friends::contains)
                .collect(Collectors.toSet());

        log.info("Пользователь {} и {} имеют {} общих друзей.", userId, friendId, intersectingIds.size());

        return intersectingIds.stream()
                .map(userStorage::getUserById)
                .collect(Collectors.toSet());

    }
}
