package ru.yandex.practicum.filmorate.storage.genre;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.mapper.GenreRowMapper;

import java.util.Collection;
import java.util.Optional;

@Component("genreDbStorage")
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {

    private final JdbcTemplate jdbcTemplate;
    private final GenreRowMapper genreRowMapper;

    @Override
    public Optional<Genre> findById(long id) {
        String sql = """
                SELECT id, name
                FROM genres
                WHERE id = ?
                """;
        return jdbcTemplate.query(sql, genreRowMapper, id)
                .stream()
                .findFirst();
    }

    @Override
    public Collection<Genre> findAll() {
        String sql = """
                SELECT id, name
                FROM genres
                ORDER BY id
                """;
        return jdbcTemplate.query(sql, genreRowMapper);
    }
}
