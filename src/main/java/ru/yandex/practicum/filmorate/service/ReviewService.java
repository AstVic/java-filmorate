package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.EventOperation;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.storage.event.EventStorage;
import ru.yandex.practicum.filmorate.storage.event.NoOpEventStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;

@Service
public class ReviewService {
    private final ReviewStorage reviewStorage;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;
    private final EventStorage eventStorage;

    @Autowired
    public ReviewService(ReviewStorage reviewStorage,
                         @Qualifier("userDbStorage") UserStorage userStorage,
                         @Qualifier("filmDbStorage") FilmStorage filmStorage,
                         EventStorage eventStorage) {
        this.reviewStorage = reviewStorage;
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
        this.eventStorage = eventStorage;
    }

    public ReviewService(ReviewStorage reviewStorage, UserStorage userStorage, FilmStorage filmStorage) {
        this(reviewStorage, userStorage, filmStorage, new NoOpEventStorage());
    }

    public Review add(Review review) {
        validate(review, false);
        requireUser(review.getUserId());
        requireFilm(review.getFilmId());
        Review created = reviewStorage.add(review);
        eventStorage.add(created.getUserId(), EventType.REVIEW, EventOperation.ADD, created.getReviewId());
        return created;
    }

    public Review update(Review review) {
        validate(review, true);
        Review existing = requireReview(review.getReviewId());
        Review updated = reviewStorage.update(review);
        eventStorage.add(existing.getUserId(), EventType.REVIEW, EventOperation.UPDATE, updated.getReviewId());
        return updated;
    }

    public void delete(long id) {
        Review existing = requireReview(id);
        reviewStorage.delete(id);
        eventStorage.add(existing.getUserId(), EventType.REVIEW, EventOperation.REMOVE, id);
    }

    public Review getById(long id) {
        return requireReview(id);
    }

    public Collection<Review> findAll(Long filmId, int count) {
        if (count <= 0) {
            throw new ValidationException("Количество отзывов должно быть положительным");
        }
        if (filmId != null) {
            requireFilm(filmId);
        }
        return reviewStorage.findAll(filmId, count);
    }

    public Review addVote(long reviewId, long userId, boolean isLike) {
        requireReview(reviewId);
        requireUser(userId);
        reviewStorage.setVote(reviewId, userId, isLike);
        return requireReview(reviewId);
    }

    public Review removeVote(long reviewId, long userId) {
        requireReview(reviewId);
        requireUser(userId);
        reviewStorage.removeVote(reviewId, userId);
        return requireReview(reviewId);
    }

    private Review requireReview(long id) {
        return reviewStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Отзыв не найден"));
    }

    private void requireUser(long id) {
        if (userStorage.findById(id).isEmpty()) {
            throw new NotFoundException("Пользователь не найден");
        }
    }

    private void requireFilm(long id) {
        if (filmStorage.findById(id).isEmpty()) {
            throw new NotFoundException("Фильм не найден");
        }
    }

    private void validate(Review review, boolean requireId) {
        if (requireId && review.getReviewId() == null) {
            throw new ValidationException("Id отзыва должен быть указан");
        }
        if (review.getContent() == null || review.getContent().isBlank()) {
            throw new ValidationException("Текст отзыва не может быть пустым");
        }
        if (review.getIsPositive() == null) {
            throw new ValidationException("Тип отзыва должен быть указан");
        }
        if (!requireId && (review.getUserId() == null || review.getFilmId() == null)) {
            throw new ValidationException("Пользователь и фильм должны быть указаны");
        }
    }
}
