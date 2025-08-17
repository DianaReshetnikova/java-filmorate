package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.validation.UserValidation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();

    @Override
    public Collection<User> getUsers() {
        return users.values();
    }

    @Override
    public User createUser(User newUser) {
        try {
            UserValidation.validateUser(newUser);
            newUser.setId(getNextId());
            users.put(newUser.getId(), newUser);
            log.info("Добавлен пользователь {}.", newUser);
            return newUser;
        } catch (ValidationException ex) {
            log.debug(ex.getMessage());
            throw ex;
        }
    }

    @Override
    public User updateUser(User newUser) {
        try {
            validateUserId(newUser.getId());
            UserValidation.validateUser(newUser);
            users.put(newUser.getId(), newUser);
            log.info("Обновлен пользователь: {}.", newUser);
            return newUser;
        } catch (ValidationException | NotFoundException ex) {
            log.debug(ex.getMessage());
            throw ex;
        }
    }

    @Override
    public void deleteUser(Long id) {
        try {
            validateUserId(id);
            users.remove(id);
            log.info("Удален пользователь: {}.", id);
        } catch (ValidationException | NotFoundException ex) {
            log.debug(ex.getMessage());
            throw ex;
        }
    }

    @Override
    public User getUserById(Long id) {
        try {
            validateUserId(id);
            log.info("Запрошен пользователь: {}.", id);
            return users.get(id);
        } catch (ValidationException | NotFoundException ex) {
            log.debug(ex.getMessage());
            throw ex;
        }
    }


    private void validateUserId(Long id){
        if (id == null)
            throw new ValidationException("Id пользователя должен быть указан");
        if (!users.containsKey(id))
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
