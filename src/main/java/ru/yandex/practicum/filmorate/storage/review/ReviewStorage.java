package ru.yandex.practicum.filmorate.storage.review;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.Collection;
import java.util.Optional;

public interface ReviewStorage {
    Review add(Review review);

    Review update(Review review);

    void delete(long id);

    Optional<Review> findById(long id);

    Collection<Review> findAll(Long filmId, int count);

    void setVote(long reviewId, long userId, boolean isLike);

    void removeVote(long reviewId, long userId);
}
