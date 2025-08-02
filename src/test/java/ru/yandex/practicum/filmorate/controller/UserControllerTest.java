package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserControllerTest {
    private final UserController userController = new UserController();

    @Test
    void shouldNotCreateUserWithLoginContainsSpaces() {
        User user = User.builder()
                .email("user.email@yandex.ru")
                .login("user Login")
                .name("user")
                .birthday(LocalDate.now())
                .build();

        ValidationException exception = assertThrows(ValidationException.class, () ->
                userController.createUser(user));
        assertNotNull(exception, "Логин не может содержать пробелы");
        assertEquals(0, userController.getUsers().stream().toList().size(), "Коллекция пользователей должна содержать 0 элементов");
    }

    @Test
    void shouldCreateUserWithEmptyName() {
        User user = User.builder()
                .email("user.email@yandex.ru")
                .login("userLogin")
                .birthday(LocalDate.now())
                .build();

        assertDoesNotThrow(() -> userController.createUser(user), "Пользователь с переданным пустым именем должен быть создан и значение взять из логина");
        assertEquals(1, userController.getUsers().stream().toList().size(), "Коллекция пользователей должна содержать 1 элементов");

        User createdUser = userController.getUsers().stream().toList().getFirst();
        boolean isEqual = user.getEmail().equals(createdUser.getEmail()) &&
                user.getLogin().equals(createdUser.getLogin()) &&
                user.getBirthday().equals(createdUser.getBirthday()) &&
                createdUser.getName().equals(user.getName());
        assertTrue(isEqual, "Поля пользователя переданного в метод для создания и уже созданного из списка должны совпадать");
    }

    @Test
    void shouldCreateUserWithTodayBirthday() {
        User user = User.builder()
                .email("user.email@yandex.ru")
                .login("userLogin")
                .name("user")
                .birthday(LocalDate.now())
                .build();

        assertDoesNotThrow(() -> userController.createUser(user), "Пользователь с сегодняшней датой рождения должен быть создан");
        assertEquals(1, userController.getUsers().stream().toList().size(), "Коллекция пользователей должна содержать 1 элементов");
    }

    @Test
    void shouldCreateUserWithPastBirthday() {
        User user = User.builder()
                .email("user.email@yandex.ru")
                .login("userLogin")
                .name("user")
                .birthday(LocalDate.now().minusYears(5).minusMonths(5))
                .build();

        assertDoesNotThrow(() -> userController.createUser(user), "Пользователь с датой рождения в прошлом должен быть создан");
        assertEquals(1, userController.getUsers().stream().toList().size(), "Коллекция пользователей должна содержать 1 элементов");
    }

    @Test
    void shouldCreateUserWithCorrectEmail() {
        User user = User.builder()
                .email("user.email@yandex.ru")
                .login("userLogin")
                .name("user")
                .birthday(LocalDate.now())
                .build();

        assertDoesNotThrow(() -> userController.createUser(user), "Пользователь с корректной электронной почтой должен быть создан");
        assertEquals(1, userController.getUsers().stream().toList().size(), "Коллекция пользователей должна содержать 1 элементов");
    }


    @Test
    void get2UsersWithCorrectData() {
        User user1 = User.builder()
                .email("user1.email@yandex.ru")
                .login("user1Login")
                .name("user1")
                .birthday(LocalDate.now())
                .build();

        User user2 = User.builder()
                .email("user2.email@yandex.ru")
                .login("user2Login")
                .name("user2")
                .birthday(LocalDate.now())
                .build();

        userController.createUser(user1);
        userController.createUser(user2);

        assertEquals(2, userController.getUsers().stream().toList().size(), "Коллекция пользователей должна содержать 2 элемента");

        User createdUser1 = userController.getUsers().stream().toList().getFirst();
        User createdUser2 = userController.getUsers().stream().toList().getLast();

        assertTrue(equalsUserFieldsWithoutId(user1, createdUser1), "Поля первого пользователя не совпадают с извлеченным из списка");
        assertTrue(equalsUserFieldsWithoutId(user2, createdUser2), "Поля второго пользователя не совпадают с извлеченным из списка");
    }


    @Test
    void shouldNotUpdateUserWithNullId() {
        User user = User.builder()
                .email("user.email@yandex.ru")
                .login("userLogin")
                .name("user")
                .birthday(LocalDate.now())
                .build();

        userController.createUser(user);

        ValidationException exception = assertThrows(ValidationException.class, () ->
                userController.updateUser(user.toBuilder()
                        .id(null)
                        .name("New user name")
                        .build()));
        assertNotNull(exception, "Пользователь с переданным значением id = null не должен быть обновлен.");
    }

    @Test
    void shouldNotUpdateUserWithNotExistingId() {
        NotFoundException exception = assertThrows(NotFoundException.class, () ->
                userController.updateUser(User.builder()
                        .id(888888888L)
                        .name("Updated new name")
                        .build()));
        assertNotNull(exception, "Пользователь с несуществующим значением id не должен быть обновлен.");
    }


    @Test
    void shouldNotUpdateUserWithLoginContainsSpaces() {
        User user = User.builder()
                .email("user.email@yandex.ru")
                .login("userLogin")
                .name("user")
                .birthday(LocalDate.now())
                .build();

        User userCreated = userController.createUser(user);

        ValidationException exception = assertThrows(ValidationException.class, () ->
                userController.updateUser(user.toBuilder()
                        .id(userCreated.getId())
                        .login("Login with spaces")
                        .build()));
        assertNotNull(exception, "Пользователь не может быть обновлен, т.к. логин не должен содержать пробелы");
    }

    private boolean equalsUserFieldsWithoutId(User user1, User user2) {
        return user1.getEmail().equals(user2.getEmail()) &&
                user1.getLogin().equals(user2.getLogin()) &&
                user1.getName().equals(user2.getName()) &&
                user1.getBirthday().equals(user2.getBirthday());
    }
}