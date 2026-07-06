package ru.yandex.practicum.filmorate.storage.event;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventOperation;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.storage.mapper.EventRowMapper;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.Instant;
import java.util.Collection;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class EventDbStorage implements EventStorage {
    private static final String INSERT_EVENT_QUERY = """
            INSERT INTO events (event_timestamp, user_id, event_type, operation, entity_id)
            VALUES (?, ?, ?, ?, ?)
            """;
    private static final String FIND_USER_EVENTS_QUERY = """
            SELECT event_id, event_timestamp, user_id, event_type, operation, entity_id
            FROM events
            WHERE user_id = ?
            ORDER BY event_id
            """;

    private final JdbcTemplate jdbcTemplate;
    private final EventRowMapper eventRowMapper;

    @Override
    public Event add(long userId, EventType eventType, EventOperation operation, long entityId) {
        long timestamp = Instant.now().toEpochMilli();
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(INSERT_EVENT_QUERY,
                    Statement.RETURN_GENERATED_KEYS);
            statement.setLong(1, timestamp);
            statement.setLong(2, userId);
            statement.setString(3, eventType.name());
            statement.setString(4, operation.name());
            statement.setLong(5, entityId);
            return statement;
        }, keyHolder);

        return new Event(Objects.requireNonNull(keyHolder.getKey()).longValue(), timestamp, userId,
                eventType, operation, entityId);
    }

    @Override
    public Collection<Event> findByUserId(long userId) {
        return jdbcTemplate.query(FIND_USER_EVENTS_QUERY, eventRowMapper, userId);
    }
}
