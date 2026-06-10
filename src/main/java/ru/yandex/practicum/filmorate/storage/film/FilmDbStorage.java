package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.storage.mapper.FilmRowMapper;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Component("filmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private static final MPA DEFAULT_MPA = MPA.G;
    private final JdbcTemplate jdbcTemplate;
    private final FilmRowMapper filmRowMapper;

    @Override
    public Film add(Film film) {
        String sql = """
                INSERT INTO films (name, description, release_date, duration, mpa_id)
                VALUES (?, ?, ?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        MPA mpa = getMpaOrDefault(film);

        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, film.getName());
            statement.setString(2, film.getDescription());
            statement.setObject(3, film.getReleaseDate());
            statement.setObject(4, film.getDuration());
            statement.setLong(5, findMpaId(mpa));
            return statement;
        }, keyHolder);

        film.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        film.setMpa(mpa);
        saveFilmGenres(film);
        saveLikes(film);
        return film;
    }

    @Override
    public Film update(Film film) {
        String sql = """
                UPDATE films
                SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ?
                WHERE id = ?
                """;
        MPA mpa = getMpaOrDefault(film);
        jdbcTemplate.update(
                sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                findMpaId(mpa),
                film.getId()
        );
        film.setMpa(mpa);
        saveFilmGenres(film);
        saveLikes(film);
        return film;
    }

    @Override
    public void delete(long id) {
        jdbcTemplate.update("DELETE FROM films WHERE id = ?", id);
    }

    @Override
    public Optional<Film> findById(long id) {
        String sql = """
                SELECT f.id, f.name, f.description, f.release_date, f.duration, m.rating
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
                SELECT f.id, f.name, f.description, f.release_date, f.duration, m.rating
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
                SELECT genre_id
                FROM film_genres
                WHERE film_id = ?
                ORDER BY genre_id
                """;
        return new HashSet<>(jdbcTemplate.query(sql, (resultSet, rowNum) -> mapGenre(resultSet.getLong("genre_id")), filmId));
    }

    private void saveLikes(Film film) {
        jdbcTemplate.update("DELETE FROM likes WHERE film_id = ?", film.getId());

        String sql = """
                INSERT INTO likes (film_id, user_id)
                VALUES (?, ?)
                """;
        film.getLikes()
                .forEach(userId -> jdbcTemplate.update(sql, film.getId(), userId));
    }

    private void saveFilmGenres(Film film) {
        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());

        String sql = """
                INSERT INTO film_genres (film_id, genre_id)
                VALUES (?, ?)
                """;
        film.getGenres()
                .forEach(genre -> jdbcTemplate.update(sql, film.getId(), getGenreId(genre)));
    }

    private long findMpaId(MPA mpa) {
        String sql = """
                SELECT id
                FROM mpa
                WHERE rating = ?
                """;
        return jdbcTemplate.queryForObject(sql, Long.class, formatMpaRating(mpa));
    }

    private MPA getMpaOrDefault(Film film) {
        if (film.getMpa() == null) {
            return DEFAULT_MPA;
        }
        return film.getMpa();
    }

    private String formatMpaRating(MPA mpa) {
        return mpa.name().replace('_', '-');
    }

    private Genre mapGenre(long genreId) {
        return Genre.values()[(int) genreId - 1];
    }

    private long getGenreId(Genre genre) {
        return genre.ordinal() + 1L;
    }
}
