package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private Map<Long, Film> films = new HashMap<>();

    private static final Instant CINEMA_BIRTHDAY = LocalDate
            .of(1895, 12, 28)
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant();

    public void check(Film film) throws ValidationException {
        if (film.getName() == null || film.getName().isBlank()) {
            log.error("Ошибка валидации: название фильма пустое или null");
            throw new ValidationException("Название фильма не может быть пустым");
        }

        if (film.getDescription() != null && film.getDescription().length() > 200) {
            log.error("Ошибка валидации: описание фильма слишком длинное ({} символов)", film.getDescription().length());
            throw new ValidationException("Описание фильма должно быть меньше 200 символов");
        }

        if (film.getReleaseDate() == null) {
            log.error("Ошибка валидации: дата релиза не указана");
            throw new ValidationException("Дата релиза должна быть указана");
        }
        if (film.getReleaseDate().isBefore(CINEMA_BIRTHDAY)) {
            log.error("Ошибка валидации: дата релиза {} раньше 28.12.1895", film.getReleaseDate());
            throw new ValidationException("Дата релиза не может быть раньше 28.12.1895");
        }

        if (film.getDuration() == null) {
            log.error("Ошибка валидации: длительность не указана");
            throw new ValidationException("Длительность фильма должна быть указана");
        }
        if (film.getDuration().isNegative() || film.getDuration().isZero()) {
            log.error("Ошибка валидации: длительность {} некорректна", film.getDuration());
            throw new ValidationException("Длительность фильма должна быть положительным числом");
        }
    }

    @PostMapping
    public Film create(@RequestBody Film film) throws ValidationException {
        log.info("Получен запрос на создание фильма: {}", film);
        check(film);
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Фильм успешно создан с id {}: {}", film.getId(), film);
        return film;
    }

    @PutMapping
    public Film update(@RequestBody Film newFilm) throws ValidationException {
        log.info("Получен запрос на обновление фильма с id {}: {}", newFilm.getId(), newFilm);

        if (newFilm.getId() == null) {
            log.error("Ошибка обновления: id фильма не указан");
            throw new ValidationException("Id должен быть указан");
        }

        if (!films.containsKey(newFilm.getId())) {
            log.error("Ошибка обновления: фильм с id {} не найден", newFilm.getId());
            throw new ValidationException("Фильм не найден");
        }

        Film film = films.get(newFilm.getId());

        if (newFilm.getName() != null) {
            film.setName(newFilm.getName());
        }
        if (newFilm.getDescription() != null) {
            film.setDescription(newFilm.getDescription());
        }
        if (newFilm.getDuration() != null) {
            film.setDuration(newFilm.getDuration());
        }
        if (newFilm.getReleaseDate() != null) {
            film.setReleaseDate(newFilm.getReleaseDate());
        }

        check(film);
        log.info("Фильм с id {} успешно обновлен: {}", film.getId(), film);
        return film;
    }

    @GetMapping
    public Collection<Film> findAll() {
        log.info("Получен запрос на получение всех фильмов");
        Collection<Film> allFilms = films.values();
        log.info("Возвращено {} фильмов", allFilms.size());
        return allFilms;
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}