package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.storage.director.DirectorDbStorage;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTest {

    private FilmController filmController;
    private UserStorage userStorage;

    @BeforeEach
    void setUp() {
        FilmStorage filmStorage = new InMemoryFilmStorage();
        userStorage = new InMemoryUserStorage();
        DirectorDbStorage directorDbStorage = new DirectorDbStorage(null, null);
        FilmService filmService = new FilmService(filmStorage, userStorage,directorDbStorage);
        filmController = new FilmController(filmService);

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
    void shouldAddLikeOnlyOncePerUser() {
        User user = createUser("user1");
        Film film = createFilm("film1");

        filmController.addLike(film.getId(), user.getId());
        filmController.addLike(film.getId(), user.getId());

        Film updatedFilm = filmController.findAll().iterator().next();
        assertEquals(1, updatedFilm.getLikes().size());
    }

    @Test
    void shouldRemoveLike() {
        User user = createUser("user1");
        Film film = createFilm("film1");
        filmController.addLike(film.getId(), user.getId());

        filmController.removeLike(film.getId(), user.getId());

        Film updatedFilm = filmController.findAll().iterator().next();
        assertTrue(updatedFilm.getLikes().isEmpty());
    }

    @Test
    void shouldReturnPopularFilmsSortedByLikes() {
        User user1 = createUser("user1");
        User user2 = createUser("user2");
        Film first = createFilm("first");
        Film second = createFilm("second");

        filmController.addLike(first.getId(), user1.getId());
        filmController.addLike(first.getId(), user2.getId());
        filmController.addLike(second.getId(), user1.getId());

        List<Film> popular = (List<Film>) filmController.getPopular(10);

        assertEquals(2, popular.size());
        assertEquals(first.getId(), popular.get(0).getId());
        assertEquals(second.getId(), popular.get(1).getId());
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
        Film createdFilm = createFilm("Film");

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

        assertThrows(NotFoundException.class, () -> filmController.update(film));
    }

    @Test
    void shouldDeleteExistingFilm() {
        Film createdFilm = createFilm("Film");
        filmController.delete(createdFilm.getId());

        assertEquals(0, filmController.findAll().size());
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistingFilm() {
        assertThrows(NotFoundException.class, () -> filmController.delete(999L));
    }


    @Test
    void shouldReturnFilmById() {
        Film createdFilm = createFilm("lookup");

        Film foundFilm = filmController.getById(createdFilm.getId());

        assertEquals(createdFilm.getId(), foundFilm.getId());
        assertEquals("lookup", foundFilm.getName());
    }

    @Test
    void shouldThrowNotFoundWhenGettingFilmByUnknownId() {
        assertThrows(NotFoundException.class, () -> filmController.getById(999L));
    }

    @Test
    void shouldReturnTop10ByDefaultCount() {
        User user = createUser("single");
        Film film = createFilm("popular");
        filmController.addLike(film.getId(), user.getId());

        List<Film> popular = (List<Film>) filmController.getPopular(10);

        assertFalse(popular.isEmpty());
    }

    private User createUser(String login) {
        User user = new User();
        user.setEmail(login + "@mail.ru");
        user.setLogin(login);
        user.setName(login);
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return userStorage.add(user);
    }

    private Film createFilm(String name) {
        Film film = new Film();
        film.setName(name);
        film.setDescription("description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(100);
        return filmController.create(film);
    }
}