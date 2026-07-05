package ru.yandex.practicum.filmorate.storage.event;

import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventOperation;
import ru.yandex.practicum.filmorate.model.EventType;

import java.util.Collection;

public interface EventStorage {
    Event add(long userId, EventType eventType, EventOperation operation, long entityId);

    Collection<Event> findByUserId(long userId);
}
