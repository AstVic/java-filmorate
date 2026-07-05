package ru.yandex.practicum.filmorate.storage.event;

import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventOperation;
import ru.yandex.practicum.filmorate.model.EventType;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

public class NoOpEventStorage implements EventStorage {
    @Override
    public Event add(long userId, EventType eventType, EventOperation operation, long entityId) {
        return new Event(null, Instant.now().toEpochMilli(), userId, eventType, operation, entityId);
    }

    @Override
    public Collection<Event> findByUserId(long userId) {
        return List.of();
    }
}
