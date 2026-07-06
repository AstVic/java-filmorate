package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Optional;
import java.util.List;

public interface FilmStorage {
    Film add(Film film);

    Film update(Film film);

    void delete(long id);

    Film addLike(long filmId, long userId);

    Film removeLike(long filmId, long userId);

    Optional<Film> findById(long id);

    Collection<Film> findAll();

    Collection<Film> findRecommendations(long userId);

    List<Film> findPopular(int count, Long genreId, Integer year);

    List<Film> search(String query, List<String> by);

    Collection<Film> findByDirector(long directorId, String sortBy);
}
