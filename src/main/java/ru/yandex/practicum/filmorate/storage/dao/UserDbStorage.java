package ru.yandex.practicum.filmorate.storage.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.interfaces.UserStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

//класс DAO (data access object) объект доступа к данным в БД к таблице users
@Repository
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<User> userRowMapper = (rs, rowNum) -> {
        User user = new User();

        user.setId(rs.getLong("id"));
        user.setEmail(rs.getString("email"));
        user.setLogin(rs.getString("login"));
        user.setName(rs.getString("name"));
        Date birthday = rs.getDate("birthday");
        user.setBirthday(birthday.toLocalDate());

        Set<Long> friendsIds = new HashSet<>(getFriendsIdsOfUser(user.getId()));
        user.setFriendsIds(friendsIds);

        return user;
    };

    @Override
    public Collection<User> getUsers() {
        String findAllQuery = "SELECT * FROM users";
        return jdbcTemplate.query(findAllQuery, userRowMapper);
    }

    @Override
    public Optional<User> getUserById(Long id) {
        String findByIdQuery = "SELECT * FROM users WHERE id = ?";
        try {
            User result = jdbcTemplate.queryForObject(findByIdQuery, userRowMapper, id);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    @Override
    public User createUser(User newUser) {
        String insertQuery = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection
                    .prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, newUser.getEmail());
            ps.setString(2, newUser.getLogin());
            ps.setString(3, newUser.getName());
            ps.setDate(4, Date.valueOf(newUser.getBirthday()));
            return ps;
        }, keyHolder);

        Long id = keyHolder.getKeyAs(Long.class);
        //сгенерированный id нового пользователя
        newUser.setId(id);

        saveFriendsOfUser(newUser.getFriendsIds(), newUser.getId());

        return newUser;
    }

    @Override
    public User updateUser(User newUser) {
        String updateQuery = "UPDATE users SET email = ?," +
                " login = ?," +
                " name = ?," +
                " birthday = ?" +
                " WHERE id = ?";

        jdbcTemplate.update(updateQuery,
                newUser.getEmail(),
                newUser.getLogin(),
                newUser.getName(),
                newUser.getBirthday(),
                newUser.getId());

        saveFriendsOfUser(newUser.getFriendsIds(), newUser.getId());

        return newUser;
    }

    @Override
    public void deleteUser(Long id) {
        String deleteByIdQuery = "DELETE FROM users WHERE id = ?";
        jdbcTemplate.update(deleteByIdQuery, id);
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        String insertNewFriendQuery = "INSERT INTO friendship (user_id, friend_id) VALUES (?, ?)";
        if (!isFriendAlreadyExist(userId, friendId)) {
            jdbcTemplate.update(insertNewFriendQuery, userId, friendId);
        }
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        String deleteFriendQuery = "DELETE FROM friendship WHERE user_id = ? AND friend_id = ?";

        if (isFriendAlreadyExist(userId, friendId)) {
            jdbcTemplate.update(deleteFriendQuery, userId, friendId);
        }
    }

    @Override
    public Collection<User> getFriendsOfUser(Long id) {
        String findAllFriendsQuery = "SELECT * FROM users AS u" +
                " JOIN friendship AS f" +
                " ON u.id = f.friend_id" +
                " WHERE f.user_id = ?";
        return jdbcTemplate.query(findAllFriendsQuery, userRowMapper, id);
    }

    @Override
    public Collection<User> getIntersectingFriends(Long userId, Long friendId) {
        String findAllIntersectingFriendsQuery = "SELECT * FROM users" +
                " WHERE id IN (SELECT friend_id FROM friendship WHERE user_id = ?)" +
                " AND id IN (SELECT friend_id FROM friendship WHERE user_id = ?)";
        return jdbcTemplate.query(findAllIntersectingFriendsQuery, userRowMapper, userId, friendId);
    }

    private List<Long> getFriendsIdsOfUser(Long id) {
        String selectFriendsIdsQuery = "SELECT friend_id FROM friendship WHERE user_id = ?";
        return jdbcTemplate.queryForList(selectFriendsIdsQuery, Long.class, id);
    }

    private void saveFriendsOfUser(Set<Long> userIdsFriends, Long userId) {
        String deleteByIdQuery = "DELETE FROM friendship WHERE user_id = ?";
        jdbcTemplate.update(deleteByIdQuery, userId);

        String insertByIdQuery = "INSERT INTO friendship WHERE user_id = ? AND friend_id = ?";
        for (var friendId : userIdsFriends) {
            jdbcTemplate.update(insertByIdQuery, userId, friendId);
        }
    }

    private boolean isFriendAlreadyExist(Long userId, Long friendId) {
        String isFriendAlreadyExistQuery = "SELECT COUNT(*) FROM friendship" +
                " WHERE user_id = ?" +
                " AND friend_id = ?";
        try {
            int result = jdbcTemplate.queryForObject(isFriendAlreadyExistQuery, Integer.class, userId, friendId);
            return result > 0;
        } catch (EmptyResultDataAccessException ignored) {
            return false;
        }
    }
}
