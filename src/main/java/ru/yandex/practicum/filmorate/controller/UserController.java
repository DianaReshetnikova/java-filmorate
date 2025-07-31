package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
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
    public User createUser(@RequestBody User newUser) {
        try {
            validateUser(newUser);

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
    public User updateUser(@RequestBody User newUser) {
        try {
            if (newUser.getId() == null) {
                throw new ValidationException("Id пользователя должен быть указан");
            }
            if (users.containsKey(newUser.getId())) {
                validateUser(newUser);

                users.put(newUser.getId(), newUser);
                log.info("Обновлен пользователь: {}.", newUser);
                return newUser;
            }
            throw new NotFoundException("Пользователь с id = " + newUser.getId() + " не найден");
        } catch (ValidationException | NotFoundException ex) {
            log.debug(ex.getMessage());
            throw ex;
        }
    }

    private void validateUser(User newUser) throws ValidationException {
        if (newUser.getEmail() == null || newUser.getEmail().isBlank() || !newUser.getEmail().contains("@"))
            throw new ValidationException("Электронная почта не может быть пустой и должна содержать символ @");
        if (newUser.getLogin() == null || newUser.getLogin().isBlank() || newUser.getLogin().contains(" "))
            throw new ValidationException("Логин не может быть пустым и содержать пробелы;");
        if (newUser.getBirthday().isAfter(LocalDate.now()))
            throw new ValidationException("Дата рождения не может быть в будущем");

        if (newUser.getName() == null || newUser.getName().isBlank())
            newUser.setName(newUser.getLogin());
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
