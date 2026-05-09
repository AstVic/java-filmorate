package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.time.LocalDate;
import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final FilmStorage filmStorage;
    private final FilmService filmService;
    private static final LocalDate CINEMA_BIRTHDAY = LocalDate.of(1895, 12, 28);

    public FilmController(FilmStorage filmStorage, FilmService filmService) {
        this.filmStorage = filmStorage;
        this.filmService = filmService;
    }

    @PostMapping
    public Film create(@RequestBody Film film) {
        log.info("Получен запрос на создание фильма: {}", film);

        check(film);

        Film createdFilm = filmStorage.add(film);

        log.info("Фильм успешно создан с id {}: {}", createdFilm.getId(), createdFilm);
        return createdFilm;
    }

    @PutMapping
    public Film update(@RequestBody Film newFilm) {
        log.info("Получен запрос на обновление фильма: {}", newFilm);

        if (newFilm.getId() == null) {
            log.error("Ошибка обновления: id фильма не указан");
            throw new ValidationException("Id должен быть указан");
        }

        if (filmStorage.findById(newFilm.getId()).isEmpty()) {
            log.error("Ошибка обновления: фильм с id {} не найден", newFilm.getId());
            throw new ValidationException("Фильм не найден");
        }

        check(newFilm);
        Film updatedFilm = filmStorage.update(newFilm);

        log.info("Фильм с id {} успешно обновлен: {}", updatedFilm.getId(), updatedFilm);
        return updatedFilm;
    }

    @PutMapping("/{id}/like/{userId}")
    public Film addLike(@PathVariable long id, @PathVariable long userId) {
        return filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public Film removeLike(@PathVariable long id, @PathVariable long userId) {
        return filmService.removeLike(id, userId);
    }

    @GetMapping("/popular")
    public Collection<Film> getPopular(@RequestParam(defaultValue = "10") int count) {
        return filmService.getPopular(count);
    }

    @GetMapping
    public Collection<Film> findAll() {
        log.info("Получен запрос на получение всех фильмов");
        return filmStorage.findAll();
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable long id) {
        log.info("Получен запрос на удаление фильм с id {}", id);

        if (filmStorage.findById(id).isEmpty()) {
            log.error("Ошибка удаления: Фильм с id {} не найден", id);
            throw new ValidationException("Фильм не найден");
        }

        filmStorage.delete(id);
        log.info("Фильм с id {} успешно удален", id);
    }


    private void check(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            log.error("Ошибка валидации: название фильма пустое");
            throw new ValidationException("Название фильма не может быть пустым");
        }

        if (film.getDescription() != null && film.getDescription().length() > 200) {
            log.error("Ошибка валидации: описание слишком длинное");
            throw new ValidationException("Максимальная длина описания — 200 символов");
        }

        if (film.getReleaseDate() == null) {
            log.error("Ошибка валидации: дата релиза не указана");
            throw new ValidationException("Дата релиза должна быть указана");
        }

        if (film.getReleaseDate().isBefore(CINEMA_BIRTHDAY)) {
            log.error("Ошибка валидации: дата релиза раньше 28.12.1895");
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }

        if (film.getDuration() == null || film.getDuration() <= 0) {
            log.error("Ошибка валидации: некорректная длительность {}", film.getDuration());
            throw new ValidationException("Продолжительность фильма должна быть положительной");
        }
    }
}