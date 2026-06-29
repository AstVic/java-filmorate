package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mapper.UserRowMapper;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Component("userDbStorage")
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {

    private static final String INSERT_USER_QUERY = """
            INSERT INTO users (login, name, email, birthday)
            VALUES (?, ?, ?, ?)
            """;
    private static final String UPDATE_USER_QUERY = """
            UPDATE users
            SET login = ?, name = ?, email = ?, birthday = ?
            WHERE id = ?
            """;
    private static final String DELETE_USER_QUERY = "DELETE FROM users WHERE id = ?";
    private static final String FIND_USER_BY_ID_QUERY = """
            SELECT id, login, name, email, birthday
            FROM users
            WHERE id = ?
            """;
    private static final String FIND_ALL_USERS_QUERY = """
            SELECT id, login, name, email, birthday
            FROM users
            ORDER BY id
            """;
    private static final String FIND_USER_FRIENDS_QUERY = """
            SELECT friend_id, status
            FROM friendships
            WHERE user_id = ?
            """;
    private static final String DELETE_USER_FRIENDS_QUERY = "DELETE FROM friendships WHERE user_id = ?";
    private static final String INSERT_USER_FRIEND_QUERY = """
            INSERT INTO friendships (user_id, friend_id, status)
            VALUES (?, ?, ?)
            """;

    private final JdbcTemplate jdbcTemplate;
    private final UserRowMapper userRowMapper;

    @Override
    public User add(User user) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(INSERT_USER_QUERY, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, user.getLogin());
            statement.setString(2, user.getName());
            statement.setString(3, user.getEmail());
            statement.setObject(4, user.getBirthday());
            return statement;
        }, keyHolder);

        user.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        saveFriends(user);
        return user;
    }

    @Override
    public User update(User user) {
        jdbcTemplate.update(
                UPDATE_USER_QUERY,
                user.getLogin(),
                user.getName(),
                user.getEmail(),
                user.getBirthday(),
                user.getId()
        );
        saveFriends(user);
        return user;
    }

    @Override
    public void delete(long id) {
        jdbcTemplate.update(DELETE_USER_QUERY, id);
    }

    @Override
    public Optional<User> findById(long id) {
        return jdbcTemplate.query(FIND_USER_BY_ID_QUERY, userRowMapper, id)
                .stream()
                .findFirst()
                .map(this::loadFriends);
    }

    @Override
    public Collection<User> findAll() {
        Collection<User> users = jdbcTemplate.query(FIND_ALL_USERS_QUERY, userRowMapper);
        users.forEach(this::loadFriends);
        return users;
    }

    private User loadFriends(User user) {
        user.setFriends(findFriends(user.getId()));
        return user;
    }

    private Map<Long, FriendshipStatus> findFriends(long userId) {
        Map<Long, FriendshipStatus> friends = new HashMap<>();
        jdbcTemplate.query(FIND_USER_FRIENDS_QUERY, resultSet -> {
            friends.put(
                    resultSet.getLong("friend_id"),
                    FriendshipStatus.valueOf(resultSet.getString("status"))
            );
        }, userId);
        return friends;
    }

    private void saveFriends(User user) {
        jdbcTemplate.update(DELETE_USER_FRIENDS_QUERY, user.getId());

        user.getFriends()
                .forEach((friendId, status) -> jdbcTemplate.update(
                        INSERT_USER_FRIEND_QUERY,
                        user.getId(),
                        friendId,
                        status.name()
                ));
    }
}
