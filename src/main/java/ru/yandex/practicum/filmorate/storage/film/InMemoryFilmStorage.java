package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.List;

@Component
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> films = new HashMap<>();

    @Override
    public Film add(Film film) {
        film.setId(getNextId());
        film.setLikes(new HashSet<>());
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film update(Film film) {
        Film oldFilm = films.get(film.getId());
        if (oldFilm != null) {
            film.setLikes(oldFilm.getLikes());
        }
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public void delete(long id) {
        films.remove(id);
    }

    @Override
    public Film addLike(long filmId, long userId) {
        Film film = films.get(filmId);
        film.getLikes().add(userId);
        return film;
    }

    @Override
    public Film removeLike(long filmId, long userId) {
        Film film = films.get(filmId);
        film.getLikes().remove(userId);
        return film;
    }

    @Override
    public Optional<Film> findById(long id) {
        return Optional.ofNullable(films.get(id));
    }

    @Override
    public Collection<Film> findAll() {
        return films.values();
    }

    @Override
    public Collection<Film> findRecommendations(long userId) {
        Set<Long> userLikes = films.values().stream()
                .filter(film -> film.getLikes().contains(userId))
                .map(Film::getId)
                .collect(Collectors.toSet());

        Optional<Long> similarUserId = films.values().stream()
                .flatMap(film -> film.getLikes().stream())
                .filter(otherUserId -> otherUserId != userId)
                .distinct()
                .max(Comparator
                        .comparingLong((Long otherUserId) -> films.values().stream()
                                .filter(film -> film.getLikes().contains(userId)
                                        && film.getLikes().contains(otherUserId))
                                .count())
                        .thenComparing(Comparator.reverseOrder()));

        if (similarUserId.isEmpty()) {
            return Set.of();
        }
        return films.values().stream()
                .filter(film -> film.getLikes().contains(similarUserId.get()))
                .filter(film -> !userLikes.contains(film.getId()))
                .sorted(Comparator.comparingLong(Film::getId))
                .collect(Collectors.toList());
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(Long::longValue)
                .max()
                .orElse(0);

        return currentMaxId + 1;
    }

    @Override
    public List<Film> findPopular(int count, Long genreId, Integer year) {
        return List.of();
    }

    @Override
    public List<Film> search(String query, List<String> by) {
        return List.of();
    }

    @Override
    public Collection<Film> findByDirector(long directorId, String sortBy) {
        return List.of();
    }
}
