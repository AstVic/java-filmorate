package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.EventOperation;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.storage.event.EventStorage;
import ru.yandex.practicum.filmorate.storage.event.NoOpEventStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.storage.director.DirectorDbStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.List;

@Service
public class FilmService {

    private static final LocalDate CINEMA_BIRTHDAY = LocalDate.of(1895, 12, 28);

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final DirectorDbStorage directorDbStorage;
    private final EventStorage eventStorage;

    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage,
                       DirectorDbStorage directorDbStorage,
                       EventStorage eventStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.directorDbStorage = directorDbStorage;
        this.eventStorage = eventStorage;
    }

    public FilmService(FilmStorage filmStorage, UserStorage userStorage, DirectorDbStorage directorDbStorage) {
        this(filmStorage, userStorage, directorDbStorage, new NoOpEventStorage());
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
        Film film = filmStorage.addLike(filmId, userId);
        eventStorage.add(userId, EventType.LIKE, EventOperation.ADD, filmId);
        return film;
    }

    public Film removeLike(long filmId, long userId) {
        getFilmOrThrow(filmId);
        validateUser(userId);
        Film film = filmStorage.removeLike(filmId, userId);
        eventStorage.add(userId, EventType.LIKE, EventOperation.REMOVE, filmId);
        return film;
    }

    public List<Film> getPopular(int count, Long genreId, Integer year) {
        if (genreId == null && year == null) {
            return filmStorage.findAll().stream()
                    .sorted(Comparator.comparingInt((Film film) -> film.getLikes().size()).reversed())
                    .limit(count)
                    .collect(Collectors.toList());
        }
        return ((FilmDbStorage) filmStorage).findPopular(count, genreId, year);
    }

    public List<Film> search(String query, List<String> by) {
        return ((FilmDbStorage) filmStorage).search(query, by);
    }

    public Collection<Film> getCommonFilms(long userId, long friendId) {
        validateUser(userId);
        validateUser(friendId);
        return filmStorage.findAll().stream()
                .filter(film -> film.getLikes().contains(userId) && film.getLikes().contains(friendId))
                .sorted(Comparator.comparingInt((Film film) -> film.getLikes().size()).reversed())
                .collect(Collectors.toList());
    }

    public Collection<Film> getFilmsByDirector(long directorId, String sortBy) {
        directorDbStorage.findById(directorId)
                .orElseThrow(() -> new NotFoundException("Режиссёр не найден"));

        if (!"year".equals(sortBy) && !"likes".equals(sortBy)) {
            throw new ValidationException("Параметр sortBy должен быть year или likes");
        }

        return ((FilmDbStorage) filmStorage).findByDirector(directorId, sortBy);
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
