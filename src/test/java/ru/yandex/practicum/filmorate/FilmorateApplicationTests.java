package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.mapper.GenreRowMapper;
import ru.yandex.practicum.filmorate.storage.mapper.MpaRowMapper;
import ru.yandex.practicum.filmorate.storage.mapper.UserRowMapper;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;
import ru.yandex.practicum.filmorate.storage.director.DirectorDbStorage;
import ru.yandex.practicum.filmorate.storage.mapper.DirectorRowMapper;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@AutoConfigureTestDatabase
@Import({UserDbStorage.class, UserRowMapper.class, FilmDbStorage.class, FilmRowMapper.class,
        GenreDbStorage.class, GenreRowMapper.class, MpaDbStorage.class, MpaRowMapper.class,
        DirectorDbStorage.class, DirectorRowMapper.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmorateApplicationTests {
    private final UserDbStorage userStorage;
    private final FilmDbStorage filmStorage;
    private final JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM likes");
        jdbcTemplate.execute("DELETE FROM film_genres");
        jdbcTemplate.execute("DELETE FROM films");
        jdbcTemplate.execute("DELETE FROM friendships");
        jdbcTemplate.execute("DELETE FROM users");
        jdbcTemplate.execute("DELETE FROM genres");
        jdbcTemplate.execute("DELETE FROM mpa");

        jdbcTemplate.execute("ALTER TABLE users ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE films ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE mpa ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE genres ALTER COLUMN id RESTART WITH 1");

        jdbcTemplate.update("INSERT INTO mpa (rating, description) VALUES ('G', 'General audiences')");
        jdbcTemplate.update("INSERT INTO mpa (rating, description) VALUES ('PG', 'Parental guidance suggested')");
        jdbcTemplate.update("INSERT INTO mpa (rating, description) VALUES ('PG-13', 'Parents strongly cautioned')");
        jdbcTemplate.update("INSERT INTO mpa (rating, description) VALUES ('R', 'Restricted')");
        jdbcTemplate.update("INSERT INTO mpa (rating, description) VALUES ('NC-17', 'Adults only')");

        jdbcTemplate.update("INSERT INTO genres (name) VALUES ('Комедия')");
        jdbcTemplate.update("INSERT INTO genres (name) VALUES ('Драма')");
        jdbcTemplate.update("INSERT INTO genres (name) VALUES ('Мультфильм')");
        jdbcTemplate.update("INSERT INTO genres (name) VALUES ('Триллер')");
        jdbcTemplate.update("INSERT INTO genres (name) VALUES ('Документальный')");
        jdbcTemplate.update("INSERT INTO genres (name) VALUES ('Боевик')");
    }

    @Test
    void shouldAddUser() {
        User user = createUser("user-login", "user@mail.ru");

        User savedUser = userStorage.add(user);

        assertThat(savedUser.getId()).isPositive();
        assertThat(userStorage.findById(savedUser.getId()))
                .isPresent()
                .hasValueSatisfying(foundUser -> assertThat(foundUser)
                        .usingRecursiveComparison()
                        .isEqualTo(savedUser));
    }

    @Test
    void shouldUpdateUser() {
        User friend = userStorage.add(createUser("friend-login", "friend@mail.ru"));
        User user = userStorage.add(createUser("user-login", "user@mail.ru"));
        user.setLogin("updated-login");
        user.setEmail("updated@mail.ru");
        user.setName("Updated name");
        user.setBirthday(LocalDate.of(1999, 9, 9));
        user.setFriends(Map.of(friend.getId(), FriendshipStatus.UNCONFIRMED));

        User updatedUser = userStorage.update(user);

        assertThat(userStorage.findById(updatedUser.getId()))
                .isPresent()
                .hasValueSatisfying(foundUser -> assertThat(foundUser)
                        .usingRecursiveComparison()
                        .isEqualTo(updatedUser));
    }

    @Test
    void shouldDeleteUser() {
        User user = userStorage.add(createUser("user-login", "user@mail.ru"));

        userStorage.delete(user.getId());

        assertThat(userStorage.findById(user.getId())).isEmpty();
        assertThat(userStorage.findAll()).isEmpty();
    }

    @Test
    void testFindUserById() {
        User user = userStorage.add(createUser("user-login", "user@mail.ru"));

        Optional<User> userOptional = userStorage.findById(user.getId());

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(foundUser -> assertThat(foundUser)
                        .hasFieldOrPropertyWithValue("id", user.getId()));
    }

    @Test
    void shouldReturnEmptyUserWhenUserDoesNotExist() {
        assertThat(userStorage.findById(999L)).isEmpty();
    }

    @Test
    void shouldFindAllUsersOrderedById() {
        User firstUser = userStorage.add(createUser("first-login", "first@mail.ru"));
        User secondUser = userStorage.add(createUser("second-login", "second@mail.ru"));

        Collection<User> users = userStorage.findAll();

        assertThat(users)
                .extracting(User::getId)
                .containsExactly(firstUser.getId(), secondUser.getId());
    }

    @Test
    void shouldAddFilm() {
        Film film = createFilm("Film");
        film.setMpa(mpa(3));
        film.setGenres(Set.of(genre(1), genre(6)));

        Film savedFilm = filmStorage.add(film);

        assertThat(savedFilm.getId()).isPositive();
        assertThat(filmStorage.findById(savedFilm.getId()))
                .isPresent()
                .hasValueSatisfying(foundFilm -> assertThat(foundFilm)
                        .usingRecursiveComparison()
                        .isEqualTo(savedFilm));
    }

    @Test
    void shouldThrowExceptionWhenFilmMpaIsMissing() {
        Film film = createFilm("Film");
        film.setMpa(null);

        assertThatThrownBy(() -> filmStorage.add(film))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Рейтинг MPA должен быть указан");
    }

    @Test
    void shouldUpdateFilm() {
        User user = userStorage.add(createUser("user-login", "user@mail.ru"));
        Film film = filmStorage.add(createFilm("Film"));
        filmStorage.addLike(film.getId(), user.getId());
        film.setName("Updated film");
        film.setDescription("Updated description");
        film.setReleaseDate(LocalDate.of(2001, 1, 1));
        film.setDuration(125);
        film.setMpa(mpa(4));
        film.setGenres(Set.of(genre(2), genre(4)));

        Film updatedFilm = filmStorage.update(film);

        assertThat(filmStorage.findById(updatedFilm.getId()))
                .isPresent()
                .hasValueSatisfying(foundFilm -> assertThat(foundFilm)
                        .usingRecursiveComparison()
                        .isEqualTo(updatedFilm));
    }

    @Test
    void shouldAddAndRemoveFilmLike() {
        User user = userStorage.add(createUser("user-login", "user@mail.ru"));
        Film film = filmStorage.add(createFilm("Film"));

        Film likedFilm = filmStorage.addLike(film.getId(), user.getId());
        filmStorage.addLike(film.getId(), user.getId());

        assertThat(likedFilm.getLikes()).containsExactly(user.getId());
        assertThat(filmStorage.findById(film.getId()))
                .isPresent()
                .hasValueSatisfying(foundFilm -> assertThat(foundFilm.getLikes()).containsExactly(user.getId()));

        Film filmWithoutLike = filmStorage.removeLike(film.getId(), user.getId());

        assertThat(filmWithoutLike.getLikes()).isEmpty();
        assertThat(filmStorage.findById(film.getId()))
                .isPresent()
                .hasValueSatisfying(foundFilm -> assertThat(foundFilm.getLikes()).isEmpty());
    }

    @Test
    void shouldRecommendFilmsLikedByUserWithMostCommonLikes() {
        User target = userStorage.add(createUser("target", "target@mail.ru"));
        User similar = userStorage.add(createUser("similar", "similar@mail.ru"));
        User lessSimilar = userStorage.add(createUser("other", "other@mail.ru"));
        Film commonOne = filmStorage.add(createFilm("Common one"));
        Film commonTwo = filmStorage.add(createFilm("Common two"));
        Film recommendation = filmStorage.add(createFilm("Recommendation"));
        Film otherFilm = filmStorage.add(createFilm("Other film"));

        filmStorage.addLike(commonOne.getId(), target.getId());
        filmStorage.addLike(commonTwo.getId(), target.getId());
        filmStorage.addLike(commonOne.getId(), similar.getId());
        filmStorage.addLike(commonTwo.getId(), similar.getId());
        filmStorage.addLike(recommendation.getId(), similar.getId());
        filmStorage.addLike(commonOne.getId(), lessSimilar.getId());
        filmStorage.addLike(otherFilm.getId(), lessSimilar.getId());

        Collection<Film> recommendations = filmStorage.findRecommendations(target.getId());

        assertThat(recommendations)
                .extracting(Film::getId)
                .containsExactly(recommendation.getId());
    }

    @Test
    void shouldReturnNoRecommendationsWithoutCommonLikes() {
        User target = userStorage.add(createUser("target", "target@mail.ru"));
        User other = userStorage.add(createUser("other", "other@mail.ru"));
        Film targetFilm = filmStorage.add(createFilm("Target film"));
        Film otherFilm = filmStorage.add(createFilm("Other film"));
        filmStorage.addLike(targetFilm.getId(), target.getId());
        filmStorage.addLike(otherFilm.getId(), other.getId());

        assertThat(filmStorage.findRecommendations(target.getId())).isEmpty();
    }

    @Test
    void shouldDeleteFilm() {
        Film film = filmStorage.add(createFilm("Film"));

        filmStorage.delete(film.getId());

        assertThat(filmStorage.findById(film.getId())).isEmpty();
        assertThat(filmStorage.findAll()).isEmpty();
    }

    @Test
    void shouldDeleteFilmWithLikesAndGenres() {
        User user = userStorage.add(createUser("user-login", "user@mail.ru"));
        Film film = createFilm("Film");
        film.setGenres(Set.of(genre(1), genre(2)));
        Film savedFilm = filmStorage.add(film);
        filmStorage.addLike(savedFilm.getId(), user.getId());

        filmStorage.delete(savedFilm.getId());

        assertThat(filmStorage.findById(savedFilm.getId())).isEmpty();
        assertThat(countRows("likes")).isZero();
        assertThat(countRows("film_genres")).isZero();
        assertThat(userStorage.findById(user.getId())).isPresent();
    }

    @Test
    void shouldDeleteUserWithFriendshipsAndLikes() {
        User user = userStorage.add(createUser("user-login", "user@mail.ru"));
        User friend = userStorage.add(createUser("friend-login", "friend@mail.ru"));
        user.setFriends(Map.of(friend.getId(), FriendshipStatus.UNCONFIRMED));
        userStorage.update(user);
        Film film = filmStorage.add(createFilm("Film"));
        filmStorage.addLike(film.getId(), user.getId());

        userStorage.delete(user.getId());

        assertThat(userStorage.findById(user.getId())).isEmpty();
        assertThat(userStorage.findById(friend.getId())).isPresent();
        assertThat(countRows("friendships")).isZero();
        assertThat(countRows("likes")).isZero();
        assertThat(filmStorage.findById(film.getId()))
                .isPresent()
                .hasValueSatisfying(foundFilm -> assertThat(foundFilm.getLikes()).isEmpty());
    }

    @Test
    void shouldFindFilmById() {
        Film film = filmStorage.add(createFilm("Film"));

        Optional<Film> filmOptional = filmStorage.findById(film.getId());

        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(foundFilm -> assertThat(foundFilm.getId()).isEqualTo(film.getId()));
    }

    @Test
    void shouldReturnEmptyFilmWhenFilmDoesNotExist() {
        assertThat(filmStorage.findById(999L)).isEmpty();
    }

    @Test
    void shouldFindAllFilmsOrderedById() {
        Film firstFilm = filmStorage.add(createFilm("First film"));
        Film secondFilm = filmStorage.add(createFilm("Second film"));

        Collection<Film> films = filmStorage.findAll();

        assertThat(films)
                .extracting(Film::getId)
                .containsExactly(firstFilm.getId(), secondFilm.getId());
    }

    private User createUser(String login, String email) {
        User user = new User();
        user.setLogin(login);
        user.setEmail(email);
        user.setName("User Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }

    private Film createFilm(String name) {
        Film film = new Film();
        film.setName(name);
        film.setDescription("Film description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(100);
        film.setMpa(mpa(1));
        return film;
    }

    private Genre genre(long id) {
        Genre genre = new Genre();
        genre.setId(id);
        return genre;
    }

    private MPA mpa(long id) {
        MPA mpa = new MPA();
        mpa.setId(id);
        return mpa;
    }

    private int countRows(String table) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + table, Integer.class);
        return count == null ? 0 : count;
    }
}
