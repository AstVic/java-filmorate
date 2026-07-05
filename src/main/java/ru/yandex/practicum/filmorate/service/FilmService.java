package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.storage.director.DirectorDbStorage;


import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Service
public class FilmService {

    private static final LocalDate CINEMA_BIRTHDAY = LocalDate.of(1895, 12, 28);

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final DirectorDbStorage directorDbStorage;

    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage,
                       DirectorDbStorage directorDbStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.directorDbStorage = directorDbStorage;
    }

    public Film create(Film film) {
        validate(film);
        return filmStorage.add(film);
    }

    public Film update(Film newFilm) {
        if (newFilm.getId() == null) {
            throw new ValidationException("Id должен быть указан");
        }
        getFilmOrThrow(newFilm.getId());
        validate(newFilm);
        return filmStorage.update(newFilm);
    }

    public Film getById(long id) {
        return getFilmOrThrow(id);
    }

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public void delete(long id) {
        getFilmOrThrow(id);
        filmStorage.delete(id);
    }

    public Film addLike(long filmId, long userId) {
        getFilmOrThrow(filmId);
        validateUser(userId);
        return filmStorage.addLike(filmId, userId);
    }

    public Film removeLike(long filmId, long userId) {
        getFilmOrThrow(filmId);
        validateUser(userId);
        return filmStorage.removeLike(filmId, userId);
    }

    public Collection<Film> getPopular(int count) {
        return filmStorage.findAll().stream()
                .sorted(Comparator.comparingInt((Film film) -> film.getLikes().size()).reversed())
                .limit(count)
                .collect(Collectors.toList());
    }

    public Collection<Film> getFilmsByDirector(long directorId, String sortBy) {
        directorDbStorage.findById(directorId)
                .orElseThrow(() -> new NotFoundException("Режиссёр не найден"));
        return directorDbStorage.findByFilmId(directorId) != null
                ? ((FilmDbStorage) filmStorage).findByDirector(directorId, sortBy)
                : new ArrayList<>();
    }

    private Film getFilmOrThrow(long id) {
        return filmStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Фильм не найден"));
    }

    private void validateUser(long userId) {
        if (userStorage.findById(userId).isEmpty()) {
            throw new NotFoundException("Пользователь не найден");
        }
    }

    private void validate(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            throw new ValidationException("Название фильма не может быть пустым");
        }

        if (film.getDescription() != null && film.getDescription().length() > 200) {
            throw new ValidationException("Максимальная длина описания — 200 символов");
        }

        if (film.getReleaseDate() == null) {
            throw new ValidationException("Дата релиза должна быть указана");
        }

        if (film.getReleaseDate().isBefore(CINEMA_BIRTHDAY)) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }

        if (film.getDuration() == null || film.getDuration() <= 0) {
            throw new ValidationException("Продолжительность фильма должна быть положительной");
        }
    }
}
