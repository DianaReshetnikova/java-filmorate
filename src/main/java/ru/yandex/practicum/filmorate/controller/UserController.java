package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.validation.UserValidation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> getUsers() {
        return users.values();
    }

    @PostMapping
    public User createUser(@Valid @RequestBody User newUser) {
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

    @PutMapping
    public User updateUser(@Valid @RequestBody User newUser) {
        try {
            if (newUser.getId() == null)
                throw new ValidationException("Id пользователя должен быть указан");
            if (!users.containsKey(newUser.getId()))
                throw new NotFoundException("Пользователь с id = " + newUser.getId() + " не найден");

            UserValidation.validateUser(newUser);
            users.put(newUser.getId(), newUser);
            log.info("Обновлен пользователь: {}.", newUser);
            return newUser;
        } catch (ValidationException | NotFoundException ex) {
            log.debug(ex.getMessage());
            throw ex;
        }
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
