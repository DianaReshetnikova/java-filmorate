package ru.yandex.practicum.filmorate.storageTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.FilmorateApplication;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.interfaces.UserService;
import ru.yandex.practicum.filmorate.storage.interfaces.UserStorage;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = FilmorateApplication.class)
@AutoConfigureTestDatabase
public class UserStorageTests {
    @Autowired
    private UserService userService;
    @Autowired
    private UserStorage userStorage;

    @BeforeEach
    void beforeEach() {
        userStorage.deleteAll();
    }

    @Test
    public void createFilm() {
        User user = User.builder()
                .name("user")
                .birthday(LocalDate.now())
                .email("user@yandex.ru")
                .login("loginUser")
                .friendsIds(new HashSet<>())
                .build();
        User createdUser = userService.createUser(user);
        assertNotEquals(0, createdUser.getId());

        User repo = userStorage.getUserById(createdUser.getId()).orElseThrow();
        assertEquals(user.getName(), repo.getName());
    }

    @Test
    public void getUsers() {
        User user = User.builder()
                .name("user")
                .birthday(LocalDate.now())
                .email("user@yandex.ru")
                .login("loginUser")
                .friendsIds(new HashSet<>())
                .build();

        User user2 = user.toBuilder()
                .name("user 2")
                .build();

        User createdUser = userService.createUser(user);
        User createdUser2 = userService.createUser(user2);

        List<User> repo = userStorage.getUsers().stream().toList();
        assertEquals(createdUser.getId(), repo.get(0).getId());
        assertEquals(createdUser2.getId(), repo.get(1).getId());
    }

    @Test
    public void updateUser() {
        User user = User.builder()
                .name("user")
                .birthday(LocalDate.now())
                .email("user@yandex.ru")
                .login("loginUser")
                .friendsIds(new HashSet<>())
                .build();

        User userCreated = userService.createUser(user);
        User update = user.toBuilder()
                .name("new name").build();

        User userUpdated = userService.updateUser(update);

        User repo = userStorage.getUserById(userCreated.getId()).get();
        assertEquals(userCreated.getId(), userUpdated.getId());
        assertEquals(update.getName(), userUpdated.getName());
    }

    @Test
    public void deleteUser() {
        User user = User.builder()
                .name("user")
                .birthday(LocalDate.now())
                .email("user@yandex.ru")
                .login("loginUser")
                .friendsIds(new HashSet<>())
                .build();

        User userCreated = userService.createUser(user);
        User repo = userStorage.getUserById(userCreated.getId()).get();
        assertEquals(userCreated.getId(), repo.getId());

        userStorage.deleteUser(userCreated.getId());

        Optional<User> opt = userStorage.getUserById(userCreated.getId());
        assertEquals(Optional.empty(), opt);
    }

    @Test
    public void addFriendToUser() {
        User user = User.builder()
                .name("user")
                .birthday(LocalDate.now())
                .email("user@yandex.ru")
                .login("loginUser")
                .friendsIds(new HashSet<>())
                .build();

        User user2 = user.toBuilder()
                .name("user 2")
                .email("user2@yandex.ru")
                .build();

        User createdUser = userService.createUser(user);
        User createdUser2 = userService.createUser(user2);

        User repo = userStorage.getUserById(createdUser.getId()).get();
        assertEquals(createdUser.getId(), repo.getId());
        User repo2 = userStorage.getUserById(createdUser2.getId()).get();
        assertEquals(createdUser2.getId(), repo2.getId());

        userService.addFriend(createdUser.getId(), createdUser2.getId());

        assertTrue(userStorage.isFriendAlreadyExist(createdUser.getId(), createdUser2.getId()));
    }

    @Test
    public void deleteFriendFromUser() {
        User user = User.builder()
                .name("user")
                .birthday(LocalDate.now())
                .email("user@yandex.ru")
                .login("loginUser")
                .friendsIds(new HashSet<>())
                .build();

        User user2 = user.toBuilder()
                .name("user 2")
                .email("user2@yandex.ru")
                .build();

        User createdUser = userService.createUser(user);
        User createdUser2 = userService.createUser(user2);

        User repo = userStorage.getUserById(createdUser.getId()).get();
        assertEquals(createdUser.getId(), repo.getId());
        User repo2 = userStorage.getUserById(createdUser2.getId()).get();
        assertEquals(createdUser2.getId(), repo2.getId());

        userService.addFriend(createdUser.getId(), createdUser2.getId());

        assertTrue(userStorage.isFriendAlreadyExist(createdUser.getId(), createdUser2.getId()));

        userService.deleteFriend(createdUser.getId(), createdUser2.getId());
        assertFalse(userStorage.isFriendAlreadyExist(createdUser.getId(), createdUser2.getId()));
    }

    @Test
    public void getFriendsOfUser() {
        User user = User.builder()
                .name("user")
                .birthday(LocalDate.now())
                .email("user@yandex.ru")
                .login("loginUser")
                .friendsIds(new HashSet<>())
                .build();

        User friend1 = user.toBuilder()
                .name("friend 1")
                .email("friend1@yandex.ru")
                .build();

        User friend2 = user.toBuilder()
                .name("friend 2")
                .email("friend2@yandex.ru")
                .build();

        User createdUser = userService.createUser(user);
        User createdFriend1 = userService.createUser(friend1);
        User createdFriend2 = userService.createUser(friend2);

        User repo = userStorage.getUserById(createdUser.getId()).get();
        assertEquals(createdUser.getId(), repo.getId());

        User friendRepo1 = userStorage.getUserById(createdFriend1.getId()).get();
        assertEquals(createdFriend1.getId(), friendRepo1.getId());

        User friendRepo2 = userStorage.getUserById(createdFriend2.getId()).get();
        assertEquals(createdFriend2.getId(), friendRepo2.getId());

        userService.addFriend(createdUser.getId(), createdFriend1.getId());
        userService.addFriend(createdUser.getId(), createdFriend2.getId());

        List<User> friends = userStorage.getFriendsOfUser(createdUser.getId()).stream().toList();
        assertEquals(createdFriend1.getId(), friends.get(0).getId());
        assertEquals(createdFriend2.getId(), friends.get(1).getId());
    }
}
