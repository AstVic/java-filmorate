package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class DirectorService {
    private final DirectorStorage directorStorage;

    public Collection<Director> findAll() {
        return directorStorage.findAll();
    }

    public Director getById(long id) {
        return directorStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Режиссёр не найден"));
    }

    public Director add(Director director) {
        return directorStorage.add(director);
    }

    public Director update(Director director) {
        return directorStorage.update(director);
    }

    public void delete(long id) {
        directorStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Режиссёр не найден"));
        directorStorage.delete(id);
    }
}
