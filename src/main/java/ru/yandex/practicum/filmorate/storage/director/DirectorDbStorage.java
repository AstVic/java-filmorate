package ru.yandex.practicum.filmorate.storage.director;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.mapper.DirectorRowMapper;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

@Component("directorDbStorage")
@RequiredArgsConstructor
public class DirectorDbStorage implements DirectorStorage {

    private static final String FIND_ALL_DIRECTORS_QUERY = """
            SELECT id, name
            FROM directors
            ORDER BY id
            """;

    private static final String FIND_DIRECTOR_BY_ID_QUERY = """
            SELECT id, name
            FROM directors
            WHERE id = ?
            """;

    private static final String INSERT_DIRECTOR_QUERY = "INSERT INTO directors (name) VALUES (?)";

    private static final String UPDATE_DIRECTOR_QUERY = """
            UPDATE directors
            SET name = ?
            WHERE id = ?
            """;

    private static final String DELETE_DIRECTOR_QUERY = "DELETE FROM directors WHERE id = ?";

    private static final String FIND_DIRECTORS_BY_FILM_ID_QUERY = """
            SELECT d.id, d.name
            FROM directors AS d
            JOIN film_directors AS fd ON d.id = fd.director_id
            WHERE fd.film_id = ?
            ORDER BY d.id
            """;

    private final JdbcTemplate jdbcTemplate;
    private final DirectorRowMapper directorRowMapper;

    @Override
    public Collection<Director> findAll() {
        return jdbcTemplate.query(FIND_ALL_DIRECTORS_QUERY, directorRowMapper);
    }

    @Override
    public Optional<Director> findById(long id) {
        return jdbcTemplate.query(FIND_DIRECTOR_BY_ID_QUERY, directorRowMapper, id)
                .stream()
                .findFirst();
    }

    @Override
    public Director add(Director director) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    INSERT_DIRECTOR_QUERY, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, director.getName());
            return ps;
        }, keyHolder);
        director.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        return director;
    }

    @Override
    public Director update(Director director) {
        int rows = jdbcTemplate.update(
                UPDATE_DIRECTOR_QUERY,
                director.getName(), director.getId());
        if (rows == 0) {
            throw new NotFoundException("Режиссёр не найден");
        }
        return director;
    }

    @Override
    public void delete(long id) {
        jdbcTemplate.update(DELETE_DIRECTOR_QUERY, id);
    }

    public Collection<Director> findByFilmId(long filmId) {
        return jdbcTemplate.query(
                FIND_DIRECTORS_BY_FILM_ID_QUERY,
                directorRowMapper, filmId);
    }
}
