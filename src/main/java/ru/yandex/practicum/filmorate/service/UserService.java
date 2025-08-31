package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.InvalidJsonFieldException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.interfaces.UserStorage;
import ru.yandex.practicum.filmorate.validation.UserValidation;

import java.util.Collection;

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
        try {
            if (newUser.getId() != 0)
                throw new InvalidJsonFieldException("Для нового пользователя нельзя указать Id");

            UserValidation.validateUser(newUser);
            return userStorage.createUser(newUser);
        } catch (ValidationException | InvalidJsonFieldException | NotFoundException ex) {
            log.debug(ex.getMessage());
            throw ex;
        }
    }

    public User updateUser(User newUser) {
        try {
            validateUserId(newUser.getId());
            UserValidation.validateUser(newUser);

            return userStorage.updateUser(newUser);
        } catch (ValidationException | NotFoundException ex) {
            log.debug(ex.getMessage());
            throw ex;
        }
    }

    public void deleteUser(Long id) {
        try {
            validateUserId(id);
            userStorage.deleteUser(id);
        } catch (ValidationException | NotFoundException ex) {
            log.debug(ex.getMessage());
            throw ex;
        }
    }

    public User getUserById(Long id) {
        try {
            validateUserId(id);
            return userStorage.getUserById(id).get();
        } catch (ValidationException | NotFoundException ex) {
            log.debug(ex.getMessage());
            throw ex;
        }
    }

    public void addFriend(Long userId, Long friendId) {
        try {
            validateUserId(userId);
            validateUserId(friendId);

            if (userId.equals(friendId))
                throw new InvalidJsonFieldException("Нельзя добавить самого себя в друзья");

            userStorage.addFriend(userId, friendId);
        } catch (ValidationException | InvalidJsonFieldException | NotFoundException ex) {
            log.debug(ex.getMessage());
            throw ex;
        }
    }

    public void deleteFriend(Long userId, Long friendId) {
        try {
            validateUserId(userId);
            validateUserId(friendId);

            userStorage.removeFriend(userId, friendId);
        } catch (ValidationException | NotFoundException ex) {
            log.debug(ex.getMessage());
            throw ex;
        }
    }

    public Collection<User> getFriendsOfUser(Long id) {
        try {
            validateUserId(id);
            return userStorage.getFriendsOfUser(id);
        } catch (ValidationException | NotFoundException ex) {
            log.debug(ex.getMessage());
            throw ex;
        }
    }

    public Collection<User> getIntersectingFriends(Long userId, Long friendId) {
        try {
            validateUserId(userId);
            validateUserId(friendId);

            if (userId.equals(friendId))
                throw new InvalidJsonFieldException("Невозможно просмотреть общих друзей самим с собой");

            return userStorage.getIntersectingFriends(userId, friendId);
        } catch (ValidationException | InvalidJsonFieldException | NotFoundException ex) {
            log.debug(ex.getMessage());
            throw ex;
        }
    }

    private void validateUserId(Long id) throws NotFoundException, ValidationException {
        if (id == null || id < 0)
            throw new ValidationException("Id пользователя должен быть положительным и не пустым");
        if (userStorage.getUserById(id).isEmpty())
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
    }
}
