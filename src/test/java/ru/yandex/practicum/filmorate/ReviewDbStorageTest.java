package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.mapper.ReviewRowMapper;
import ru.yandex.practicum.filmorate.storage.review.ReviewDbStorage;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({ReviewDbStorage.class, ReviewRowMapper.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ReviewDbStorageTest {
    private final ReviewDbStorage reviewStorage;
    private final JdbcTemplate jdbcTemplate;
    private long firstUserId;
    private long secondUserId;
    private long filmId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("INSERT INTO mpa (rating) VALUES ('TEST')");
        jdbcTemplate.update("""
                INSERT INTO users (login, name, email, birthday)
                VALUES ('one', 'One', 'one@test.ru', '2000-01-01'),
                       ('two', 'Two', 'two@test.ru', '2000-01-01')
                """);
        firstUserId = jdbcTemplate.queryForObject(
                "SELECT id FROM users WHERE email = 'one@test.ru'", Long.class);
        secondUserId = jdbcTemplate.queryForObject(
                "SELECT id FROM users WHERE email = 'two@test.ru'", Long.class);
        Long mpaId = jdbcTemplate.queryForObject(
                "SELECT id FROM mpa WHERE rating = 'TEST'", Long.class);
        jdbcTemplate.update("""
                INSERT INTO films (name, description, release_date, duration, mpa_id)
                VALUES ('Film', 'Description', '2000-01-01', 100, ?)
                """, mpaId);
        filmId = jdbcTemplate.queryForObject(
                "SELECT id FROM films WHERE name = 'Film'", Long.class);
    }

    @Test
    void shouldCreateUpdateAndDeleteReview() {
        Review review = createReview("Original");

        Review saved = reviewStorage.add(review);
        saved.setContent("Updated");
        saved.setIsPositive(false);
        Review updated = reviewStorage.update(saved);

        assertThat(updated.getReviewId()).isPositive();
        assertThat(updated.getContent()).isEqualTo("Updated");
        assertThat(updated.getIsPositive()).isFalse();
        assertThat(updated.getUseful()).isZero();

        reviewStorage.delete(updated.getReviewId());
        assertThat(reviewStorage.findById(updated.getReviewId())).isEmpty();
    }

    @Test
    void shouldReplaceVoteAndSortReviewsByUsefulness() {
        Review first = reviewStorage.add(createReview("First"));
        Review second = reviewStorage.add(createReview("Second"));

        reviewStorage.setVote(first.getReviewId(), firstUserId, false);
        reviewStorage.setVote(first.getReviewId(), firstUserId, true);
        reviewStorage.setVote(second.getReviewId(), secondUserId, false);

        List<Review> reviews = List.copyOf(reviewStorage.findAll(filmId, 10));

        assertThat(reviews).extracting(Review::getReviewId)
                .containsExactly(first.getReviewId(), second.getReviewId());
        assertThat(reviews).extracting(Review::getUseful).containsExactly(1, -1);

        reviewStorage.removeVote(first.getReviewId(), firstUserId);
        assertThat(reviewStorage.findById(first.getReviewId()).orElseThrow().getUseful()).isZero();
    }

    private Review createReview(String content) {
        Review review = new Review();
        review.setContent(content);
        review.setIsPositive(true);
        review.setUserId(firstUserId);
        review.setFilmId(filmId);
        return review;
    }
}
