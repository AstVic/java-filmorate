package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @PostMapping
    public Review add(@RequestBody Review review) {
        log.info("POST /reviews");
        return reviewService.add(review);
    }

    @PutMapping
    public Review update(@RequestBody Review review) {
        log.info("PUT /reviews");
        return reviewService.update(review);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable long id) {
        reviewService.delete(id);
    }

    @GetMapping("/{id}")
    public Review getById(@PathVariable long id) {
        return reviewService.getById(id);
    }

    @GetMapping
    public Collection<Review> findAll(@RequestParam(required = false) Long filmId,
                                      @RequestParam(defaultValue = "10") int count) {
        return reviewService.findAll(filmId, count);
    }

    @PutMapping("/{id}/like/{userId}")
    public void like(@PathVariable long id, @PathVariable long userId) {
        reviewService.addVote(id, userId, true);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void dislike(@PathVariable long id, @PathVariable long userId) {
        reviewService.addVote(id, userId, false);
    }

    @DeleteMapping({"/{id}/like/{userId}", "/{id}/dislike/{userId}"})
    public void removeVote(@PathVariable long id, @PathVariable long userId) {
        reviewService.removeVote(id, userId);
    }
}
