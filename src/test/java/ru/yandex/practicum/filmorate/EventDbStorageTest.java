package ru.yandex.practicum.filmorate;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventOperation;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.storage.event.EventDbStorage;
import ru.yandex.practicum.filmorate.storage.mapper.EventRowMapper;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

class EventDbStorageTest {
    private EventDbStorage eventStorage;

    @BeforeEach
    void setUp() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:event-test;DB_CLOSE_DELAY=-1");
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.execute("DROP ALL OBJECTS");
        jdbcTemplate.execute("""
                CREATE TABLE users (
                    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE events (
                    event_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                    event_timestamp BIGINT NOT NULL,
                    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                    event_type VARCHAR(20) NOT NULL,
                    operation VARCHAR(20) NOT NULL,
                    entity_id BIGINT NOT NULL
                )
                """);
        jdbcTemplate.update("INSERT INTO users DEFAULT VALUES");
        eventStorage = new EventDbStorage(jdbcTemplate, new EventRowMapper());
    }

    @Test
    void shouldReturnUserEventsInCreationOrder() {
        Event first = eventStorage.add(1, EventType.FRIEND, EventOperation.ADD, 2);
        Event second = eventStorage.add(1, EventType.LIKE, EventOperation.REMOVE, 10);

        Collection<Event> events = eventStorage.findByUserId(1);

        assertThat(events).containsExactly(first, second);
        assertThat(first.getTimestamp()).isPositive();
        assertThat(first.getEventId()).isPositive();
    }

    @Test
    void shouldReturnEmptyFeedForUserWithoutEvents() {
        assertThat(eventStorage.findByUserId(1)).isEmpty();
    }
}
