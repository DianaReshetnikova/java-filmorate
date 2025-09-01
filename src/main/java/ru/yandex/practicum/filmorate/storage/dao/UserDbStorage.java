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
        String FIND_ALL_QUERY = "SELECT * FROM users";
        return jdbcTemplate.query(FIND_ALL_QUERY, userRowMapper);
    }

    @Override
    public Optional<User> getUserById(Long id) {
        String FIND_BY_ID_QUERY = "SELECT * FROM users WHERE id = ?";
        try {
            User result = jdbcTemplate.queryForObject(FIND_BY_ID_QUERY, userRowMapper, id);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    @Override
    public User createUser(User newUser) {
        String INSERT_QUERY = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection
                    .prepareStatement(INSERT_QUERY, Statement.RETURN_GENERATED_KEYS);
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
        String UPDATE_QUERY = "UPDATE users SET email = ?," +
                " login = ?," +
                " name = ?," +
                " birthday = ?" +
                " WHERE id = ?";

        jdbcTemplate.update(UPDATE_QUERY,
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
        String DELETE_BY_ID_QUERY = "DELETE FROM users WHERE id = ?";
        jdbcTemplate.update(DELETE_BY_ID_QUERY, id);
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        String INSERT_NEW_FRIEND_QUERY = "INSERT INTO friendship (user_id, friend_id) VALUES (?, ?)";
        if (!isFriendAlreadyExist(userId, friendId)) {
            jdbcTemplate.update(INSERT_NEW_FRIEND_QUERY, userId, friendId);
        }
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        String DELETE_FRIEND_QUERY = "DELETE FROM friendship WHERE user_id = ? AND friend_id = ?";

        if (isFriendAlreadyExist(userId, friendId)) {
            jdbcTemplate.update(DELETE_FRIEND_QUERY, userId, friendId);
        }
    }

    @Override
    public Collection<User> getFriendsOfUser(Long id) {
        String FIND_ALL_FRIENDS_QUERY = "SELECT * FROM users AS u" +
                " JOIN friendship AS f" +
                " ON u.id = f.friend_id" +
                " WHERE f.user_id = ?";
        return jdbcTemplate.query(FIND_ALL_FRIENDS_QUERY, userRowMapper);
    }

    @Override
    public Collection<User> getIntersectingFriends(Long userId, Long friendId) {
        String FIND_ALL_INTERSECTING_FRIENDS_QUERY = """
                SELECT u.* FROM users u
                JOIN friendship f1 ON (
                    f1.user_id = ? AND u.id = f1.friend_id) OR (f1.friend_id = ? AND u.id = f1.user_id)
                JOIN friendship f2 ON
                    (f2.user_id = ? AND u.id = f2.friend_id) OR (f2.friend_id = ? AND u.id = f2.user_id)
                """;
        return jdbcTemplate.query(FIND_ALL_INTERSECTING_FRIENDS_QUERY, userRowMapper, userId, friendId);
    }

    public List<Long> getFriendsIdsOfUser(Long id) {
        String SELECT_FRIENDS_IDS_QUERY = "SELECT friend_id FROM friendship WHERE user_id = ?";
        return jdbcTemplate.queryForList(SELECT_FRIENDS_IDS_QUERY, Long.class, id);
    }

    private void saveFriendsOfUser(Set<Long> userIdsFriends, Long userId) {
        String DELETE_BY_ID_QUERY = "DELETE FROM friendship WHERE user_id = ?";
        jdbcTemplate.update(DELETE_BY_ID_QUERY, userId);

        String INSERT_BY_ID_QUERY = "INSERT INTO friendship WHERE user_id = ? AND friend_id = ?";
        for (var friendId : userIdsFriends) {
            jdbcTemplate.update(INSERT_BY_ID_QUERY, userId, friendId);
        }
    }

    private boolean isFriendAlreadyExist(Long userId, Long friendId) {
        String IS_FRIEND_ALREADY_EXIST_QUERY = "SELECT COUNT(*) FROM friendship" +
                " WHERE user_id = ?" +
                " AND friend_id = ?";
        try {
            int result = jdbcTemplate.queryForObject(IS_FRIEND_ALREADY_EXIST_QUERY, Integer.class, userId, friendId);
            return result > 0;
        } catch (EmptyResultDataAccessException ignored) {
            return false;
        }
    }
}
