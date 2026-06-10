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

    private final JdbcTemplate jdbcTemplate;
    private final UserRowMapper userRowMapper;

    @Override
    public User add(User user) {
        String sql = """
                INSERT INTO users (login, name, email, birthday)
                VALUES (?, ?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
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
        String sql = """
                UPDATE users
                SET login = ?, name = ?, email = ?, birthday = ?
                WHERE id = ?
                """;
        jdbcTemplate.update(sql, user.getLogin(), user.getName(), user.getEmail(), user.getBirthday(), user.getId());
        saveFriends(user);
        return user;
    }

    @Override
    public void delete(long id) {
        jdbcTemplate.update("DELETE FROM users WHERE id = ?", id);
    }

    @Override
    public Optional<User> findById(long id) {
        String sql = """
                SELECT id, login, name, email, birthday
                FROM users
                WHERE id = ?
                """;
        return jdbcTemplate.query(sql, userRowMapper, id)
                .stream()
                .findFirst()
                .map(this::loadFriends);
    }

    @Override
    public Collection<User> findAll() {
        String sql = """
                SELECT id, login, name, email, birthday
                FROM users
                ORDER BY id
                """;
        Collection<User> users = jdbcTemplate.query(sql, userRowMapper);
        users.forEach(this::loadFriends);
        return users;
    }

    private User loadFriends(User user) {
        user.setFriends(findFriends(user.getId()));
        return user;
    }

    private Map<Long, FriendshipStatus> findFriends(long userId) {
        String sql = """
                SELECT friend_id, status
                FROM friendships
                WHERE user_id = ?
                """;
        Map<Long, FriendshipStatus> friends = new HashMap<>();
        jdbcTemplate.query(sql, resultSet -> {
            friends.put(
                    resultSet.getLong("friend_id"),
                    FriendshipStatus.valueOf(resultSet.getString("status"))
            );
        }, userId);
        return friends;
    }

    private void saveFriends(User user) {
        jdbcTemplate.update("DELETE FROM friendships WHERE user_id = ?", user.getId());

        String sql = """
                INSERT INTO friendships (user_id, friend_id, status)
                VALUES (?, ?, ?)
                """;
        user.getFriends()
                .forEach((friendId, status) -> jdbcTemplate.update(sql, user.getId(), friendId, status.name()));
    }
}
