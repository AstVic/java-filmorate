package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Optional;

public interface FilmStorage {
    Film add(Film film);

    Film update(Film film);

    void delete(long id);

    Film addLike(long filmId, long userId);

    Film removeLike(long filmId, long userId);

    Optional<Film> findById(long id);

    Collection<Film> findAll();

    Collection<Film> findRecommendations(long userId);
}
