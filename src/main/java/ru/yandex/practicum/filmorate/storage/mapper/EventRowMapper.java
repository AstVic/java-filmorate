package ru.yandex.practicum.filmorate.storage.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventOperation;
import ru.yandex.practicum.filmorate.model.EventType;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class EventRowMapper implements RowMapper<Event> {
    @Override
    public Event mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        return new Event(
                resultSet.getLong("event_id"),
                resultSet.getLong("event_timestamp"),
                resultSet.getLong("user_id"),
                EventType.valueOf(resultSet.getString("event_type")),
                EventOperation.valueOf(resultSet.getString("operation")),
                resultSet.getLong("entity_id")
        );
    }
}
