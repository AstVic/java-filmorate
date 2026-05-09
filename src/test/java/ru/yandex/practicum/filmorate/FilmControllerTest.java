package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTest {

    private FilmController filmController;

    @BeforeEach
    void setUp() {
        filmController = new FilmController(new InMemoryFilmStorage());

    }

    @Test
    void shouldCreateValidFilm() {
        Film film = new Film();
        film.setName("Film");
        film.setDescription("Good film");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(100);

        Film createdFilm = filmController.create(film);

        assertNotNull(createdFilm.getId());
        assertEquals("Film", createdFilm.getName());
        assertEquals("Good film", createdFilm.getDescription());
        assertEquals(LocalDate.of(2000, 1, 1), createdFilm.getReleaseDate());
        assertEquals(100, createdFilm.getDuration());
        assertEquals(1, filmController.findAll().size());
    }

    @Test
    void shouldThrowExceptionWhenFilmNameIsBlank() {
        Film film = new Film();
        film.setName("");
        film.setDescription("Good film");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(100);

        assertThrows(ValidationException.class, () -> filmController.create(film));
    }

    @Test
    void shouldThrowExceptionWhenDescriptionIsTooLong() {
        Film film = new Film();
        film.setName("Film");
        film.setDescription("a".repeat(201));
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(100);

        assertThrows(ValidationException.class, () -> filmController.create(film));
    }

    @Test
    void shouldCreateFilmWithDescriptionLength200() {
        Film film = new Film();
        film.setName("Film");
        film.setDescription("a".repeat(200));
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(100);

        Film createdFilm = filmController.create(film);

        assertNotNull(createdFilm.getId());
        assertEquals(200, createdFilm.getDescription().length());
    }

    @Test
    void shouldThrowExceptionWhenReleaseDateIsTooEarly() {
        Film film = new Film();
        film.setName("Film");
        film.setDescription("Good film");
        film.setReleaseDate(LocalDate.of(1895, 12, 27));
        film.setDuration(100);

        assertThrows(ValidationException.class, () -> filmController.create(film));
    }

    @Test
    void shouldCreateFilmWithReleaseDateAtBoundary() {
        Film film = new Film();
        film.setName("Film");
        film.setDescription("Good film");
        film.setReleaseDate(LocalDate.of(1895, 12, 28));
        film.setDuration(100);

        Film createdFilm = filmController.create(film);

        assertNotNull(createdFilm.getId());
        assertEquals(LocalDate.of(1895, 12, 28), createdFilm.getReleaseDate());
    }

    @Test
    void shouldThrowExceptionWhenDurationIsZero() {
        Film film = new Film();
        film.setName("Film");
        film.setDescription("Good film");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(0);

        assertThrows(ValidationException.class, () -> filmController.create(film));
    }

    @Test
    void shouldThrowExceptionWhenDurationIsNegative() {
        Film film = new Film();
        film.setName("Film");
        film.setDescription("Good film");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(-1);

        assertThrows(ValidationException.class, () -> filmController.create(film));
    }

    @Test
    void shouldUpdateExistingFilm() {
        Film film = new Film();
        film.setName("Film");
        film.setDescription("Good film");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(100);

        Film createdFilm = filmController.create(film);

        Film updatedFilm = new Film();
        updatedFilm.setId(createdFilm.getId());
        updatedFilm.setName("Updated film");
        updatedFilm.setDescription("Updated description");
        updatedFilm.setReleaseDate(LocalDate.of(2001, 1, 1));
        updatedFilm.setDuration(120);

        Film result = filmController.update(updatedFilm);

        assertEquals(createdFilm.getId(), result.getId());
        assertEquals("Updated film", result.getName());
        assertEquals("Updated description", result.getDescription());
        assertEquals(LocalDate.of(2001, 1, 1), result.getReleaseDate());
        assertEquals(120, result.getDuration());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingFilmWithoutId() {
        Film film = new Film();
        film.setName("Film");
        film.setDescription("Good film");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(100);

        assertThrows(ValidationException.class, () -> filmController.update(film));
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistingFilm() {
        Film film = new Film();
        film.setId(999L);
        film.setName("Film");
        film.setDescription("Good film");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(100);

        assertThrows(ValidationException.class, () -> filmController.update(film));
    }

    @Test
    void shouldDeleteExistingFilm() {
        Film film = new Film();
        film.setName("Film");
        film.setDescription("Good film");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(100);

        Film createdFilm = filmController.create(film);
        filmController.delete(createdFilm.getId());

        assertEquals(0, filmController.findAll().size());
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistingFilm() {
        assertThrows(ValidationException.class, () -> filmController.delete(999L));
    }
}