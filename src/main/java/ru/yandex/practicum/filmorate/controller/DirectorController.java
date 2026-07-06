package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/directors")
@RequiredArgsConstructor
public class DirectorController {
    private final DirectorService directorService;

    @GetMapping
    public Collection<Director> findAll() {
        log.info("GET /directors");
        return directorService.findAll();
    }

    @GetMapping("/{id}")
    public Director getById(@PathVariable long id) {
        log.info("GET /directors/{}", id);
        return directorService.getById(id);
    }

    @PostMapping
    public Director add(@RequestBody Director director) {
        log.info("POST /directors");
        return directorService.add(director);
    }

    @PutMapping
    public Director update(@RequestBody Director director) {
        log.info("PUT /directors");
        return directorService.update(director);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable long id) {
        log.info("DELETE /directors/{}", id);
        directorService.delete(id);
    }
}
