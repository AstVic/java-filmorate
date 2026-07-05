package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;

@Service
public class ReviewService {
    private final ReviewStorage reviewStorage;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    public ReviewService(ReviewStorage reviewStorage,
                         @Qualifier("userDbStorage") UserStorage userStorage,
                         @Qualifier("filmDbStorage") FilmStorage filmStorage) {
        this.reviewStorage = reviewStorage;
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
    }

    public Review add(Review review) {
        validate(review, false);
        requireUser(review.getUserId());
        requireFilm(review.getFilmId());
        return reviewStorage.add(review);
    }

    public Review update(Review review) {
        validate(review, true);
        requireReview(review.getReviewId());
        return reviewStorage.update(review);
    }

    public void delete(long id) {
        requireReview(id);
        reviewStorage.delete(id);
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

    public void addVote(long reviewId, long userId, boolean isLike) {
        requireReview(reviewId);
        requireUser(userId);
        reviewStorage.setVote(reviewId, userId, isLike);
    }

    public void removeVote(long reviewId, long userId) {
        requireReview(reviewId);
        requireUser(userId);
        reviewStorage.removeVote(reviewId, userId);
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
