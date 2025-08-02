package ru.yandex.practicum.filmorate.validation;

import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

public class UserValidation {
    public static void validateUser(User newUser) throws ValidationException {
        if (newUser.getLogin().contains(" "))
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");

        if (newUser.getName() == null || newUser.getName().isBlank())
            newUser.setName(newUser.getLogin());
    }
}
