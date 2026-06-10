package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.storage.mapper.MpaRowMapper;

import java.util.Collection;
import java.util.Optional;

@Component("mpaDbStorage")
@RequiredArgsConstructor
public class MpaDbStorage implements MpaStorage {

    private final JdbcTemplate jdbcTemplate;
    private final MpaRowMapper mpaRowMapper;

    @Override
    public Optional<MPA> findById(long id) {
        String sql = """
                SELECT id, rating, description
                FROM mpa
                WHERE id = ?
                """;
        return jdbcTemplate.query(sql, mpaRowMapper, id)
                .stream()
                .findFirst();
    }

    @Override
    public Optional<MPA> findByRating(String rating) {
        String sql = """
                SELECT id, rating, description
                FROM mpa
                WHERE rating = ?
                """;
        return jdbcTemplate.query(sql, mpaRowMapper, rating)
                .stream()
                .findFirst();
    }

    @Override
    public Collection<MPA> findAll() {
        String sql = """
                SELECT id, rating, description
                FROM mpa
                ORDER BY id
                """;
        return jdbcTemplate.query(sql, mpaRowMapper);
    }
}
