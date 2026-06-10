package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.mapper.GenreRowMapper;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Component("filmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private static final String DEFAULT_MPA_RATING = "G";
    private final JdbcTemplate jdbcTemplate;
    private final FilmRowMapper filmRowMapper;
    private final GenreRowMapper genreRowMapper;
    private final GenreStorage genreStorage;
    private final MpaStorage mpaStorage;

    @Override
    public Film add(Film film) {
        String sql = """
                INSERT INTO films (name, description, release_date, duration, mpa_id)
                VALUES (?, ?, ?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        MPA mpa = resolveMpa(film);

        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, film.getName());
            statement.setString(2, film.getDescription());
            statement.setObject(3, film.getReleaseDate());
            statement.setObject(4, film.getDuration());
            statement.setLong(5, mpa.getId());
            return statement;
        }, keyHolder);

        film.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        film.setMpa(mpa);
        film.setGenres(resolveGenres(film));
        saveFilmGenres(film);
        return findById(film.getId()).orElseThrow();
    }

    @Override
    public Film update(Film film) {
        String sql = """
                UPDATE films
                SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ?
                WHERE id = ?
                """;
        MPA mpa = resolveMpa(film);
        jdbcTemplate.update(
                sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                mpa.getId(),
                film.getId()
        );
        film.setMpa(mpa);
        film.setGenres(resolveGenres(film));
        saveFilmGenres(film);
        return findById(film.getId()).orElseThrow();
    }

    @Override
    public void delete(long id) {
        jdbcTemplate.update("DELETE FROM films WHERE id = ?", id);
    }

    @Override
    public Film addLike(long filmId, long userId) {
        String sql = """
                MERGE INTO likes (film_id, user_id)
                KEY (film_id, user_id)
                VALUES (?, ?)
                """;
        jdbcTemplate.update(sql, filmId, userId);
        return findById(filmId).orElseThrow();
    }

    @Override
    public Film removeLike(long filmId, long userId) {
        jdbcTemplate.update("DELETE FROM likes WHERE film_id = ? AND user_id = ?", filmId, userId);
        return findById(filmId).orElseThrow();
    }

    @Override
    public Optional<Film> findById(long id) {
        String sql = """
                SELECT f.id, f.name, f.description, f.release_date, f.duration,
                       m.id AS mpa_id, m.rating, m.description AS mpa_description
                FROM films AS f
                JOIN mpa AS m ON f.mpa_id = m.id
                WHERE f.id = ?
                """;
        return jdbcTemplate.query(sql, filmRowMapper, id)
                .stream()
                .findFirst()
                .map(this::loadFilmRelations);
    }

    @Override
    public Collection<Film> findAll() {
        String sql = """
                SELECT f.id, f.name, f.description, f.release_date, f.duration,
                       m.id AS mpa_id, m.rating, m.description AS mpa_description
                FROM films AS f
                JOIN mpa AS m ON f.mpa_id = m.id
                ORDER BY f.id
                """;
        Collection<Film> films = jdbcTemplate.query(sql, filmRowMapper);
        films.forEach(this::loadFilmRelations);
        return films;
    }

    private Film loadFilmRelations(Film film) {
        film.setLikes(findLikes(film.getId()));
        film.setGenres(findGenres(film.getId()));
        return film;
    }

    private Set<Long> findLikes(long filmId) {
        String sql = """
                SELECT user_id
                FROM likes
                WHERE film_id = ?
                """;
        return new HashSet<>(jdbcTemplate.query(sql, (resultSet, rowNum) -> resultSet.getLong("user_id"), filmId));
    }

    private Set<Genre> findGenres(long filmId) {
        String sql = """
                SELECT g.id, g.name
                FROM film_genres AS fg
                JOIN genres AS g ON fg.genre_id = g.id
                WHERE fg.film_id = ?
                ORDER BY g.id
                """;
        return new LinkedHashSet<>(jdbcTemplate.query(sql, genreRowMapper, filmId));
    }

    private void saveFilmGenres(Film film) {
        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());

        String sql = """
                INSERT INTO film_genres (film_id, genre_id)
                VALUES (?, ?)
                """;
        for (Genre genre : film.getGenres()) {
            jdbcTemplate.update(sql, film.getId(), genre.getId());
        }
    }

    private Set<Genre> resolveGenres(Film film) {
        Set<Genre> resolvedGenres = new LinkedHashSet<>();
        for (Genre genre : film.getGenres()) {
            Genre resolvedGenre = genreStorage.findById(genre.getId())
                    .orElseThrow(() -> new NotFoundException("Жанр не найден"));
            resolvedGenres.add(resolvedGenre);
        }
        return resolvedGenres;
    }

    private MPA resolveMpa(Film film) {
        if (film.getMpa() == null || film.getMpa().getId() == null) {
            return mpaStorage.findByRating(DEFAULT_MPA_RATING)
                    .orElseThrow(() -> new NotFoundException("Рейтинг не найден"));
        }
        return mpaStorage.findById(film.getMpa().getId())
                .orElseThrow(() -> new NotFoundException("Рейтинг не найден"));
    }
}
