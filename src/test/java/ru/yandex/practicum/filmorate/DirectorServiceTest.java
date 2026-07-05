package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class DirectorServiceTest {
    private final DirectorStorage directorStorage = mock(DirectorStorage.class);
    private final DirectorService directorService = new DirectorService(directorStorage);

    @Test
    void shouldRejectDirectorWithBlankNameOnAdd() {
        Director director = new Director();
        director.setName(" ");

        assertThrows(ValidationException.class, () -> directorService.add(director));
        verifyNoInteractions(directorStorage);
    }

    @Test
    void shouldRejectDirectorWithBlankNameOnUpdate() {
        Director director = new Director();
        director.setId(1L);
        director.setName("\t");

        assertThrows(ValidationException.class, () -> directorService.update(director));
        verifyNoInteractions(directorStorage);
    }
}
