package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public Film addLike(long filmId, long userId) {
        Film film = getFilmOrThrow(filmId);
        validateUser(userId);
        film.getLikes().add(userId);
        return filmStorage.update(film);
    }

    public Film removeLike(long filmId, long userId) {
        Film film = getFilmOrThrow(filmId);
        validateUser(userId);
        film.getLikes().remove(userId);
        return filmStorage.update(film);
    }

    public Collection<Film> getPopular(int count) {
        return filmStorage.findAll().stream()
                .sorted(Comparator.comparingInt((Film film) -> film.getLikes().size()).reversed())
                .limit(count)
                .collect(Collectors.toList());
    }

    private Film getFilmOrThrow(long id) {
        return filmStorage.findById(id)
                .orElseThrow(() -> new ValidationException("Фильм не найден"));
    }

    private void validateUser(long userId) {
        if (userStorage.findById(userId).isEmpty()) {
            throw new ValidationException("Пользователь не найден");
        }
    }
}
