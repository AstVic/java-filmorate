package ru.yandex.practicum.filmorate.storage.review;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.mapper.ReviewRowMapper;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ReviewDbStorage implements ReviewStorage {
    private static final String SELECT_REVIEW = """
            SELECT r.id AS review_id, r.content, r.is_positive, r.user_id, r.film_id,
                   COALESCE(SUM(CASE WHEN v.is_like = TRUE THEN 1
                                     WHEN v.is_like = FALSE THEN -1 ELSE 0 END), 0) AS useful
            FROM reviews r
            LEFT JOIN review_votes v ON v.review_id = r.id
            """;

    private final JdbcTemplate jdbcTemplate;
    private final ReviewRowMapper rowMapper;

    @Override
    public Review add(Review review) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO reviews (content, is_positive, user_id, film_id) VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, review.getContent());
            statement.setBoolean(2, review.getIsPositive());
            statement.setLong(3, review.getUserId());
            statement.setLong(4, review.getFilmId());
            return statement;
        }, keyHolder);
        return findById(Objects.requireNonNull(keyHolder.getKey()).longValue()).orElseThrow();
    }

    @Override
    public Review update(Review review) {
        jdbcTemplate.update("UPDATE reviews SET content = ?, is_positive = ? WHERE id = ?",
                review.getContent(), review.getIsPositive(), review.getReviewId());
        return findById(review.getReviewId()).orElseThrow();
    }

    @Override
    public void delete(long id) {
        jdbcTemplate.update("DELETE FROM reviews WHERE id = ?", id);
    }

    @Override
    public Optional<Review> findById(long id) {
        String query = SELECT_REVIEW + " WHERE r.id = ? GROUP BY r.id";
        return jdbcTemplate.query(query, rowMapper, id).stream().findFirst();
    }

    @Override
    public Collection<Review> findAll(Long filmId, int count) {
        String filter = filmId == null ? "" : " WHERE r.film_id = ?";
        String query = SELECT_REVIEW + filter + " GROUP BY r.id ORDER BY useful DESC, r.id LIMIT ?";
        return filmId == null
                ? jdbcTemplate.query(query, rowMapper, count)
                : jdbcTemplate.query(query, rowMapper, filmId, count);
    }

    @Override
    public void setVote(long reviewId, long userId, boolean isLike) {
        jdbcTemplate.update("""
                MERGE INTO review_votes (review_id, user_id, is_like)
                KEY (review_id, user_id) VALUES (?, ?, ?)
                """, reviewId, userId, isLike);
    }

    @Override
    public void removeVote(long reviewId, long userId) {
        jdbcTemplate.update("DELETE FROM review_votes WHERE review_id = ? AND user_id = ?", reviewId, userId);
    }
}
