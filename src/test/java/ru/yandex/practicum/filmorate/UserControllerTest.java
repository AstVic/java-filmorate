package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {

    private UserController userController;

    @BeforeEach
    void setUp() {
        userController = new UserController(new InMemoryUserStorage());
    }

    @Test
    void shouldCreateValidUser() {
        User user = new User();
        user.setEmail("mail@mail.ru");
        user.setLogin("dolore");
        user.setName("Nick Name");
        user.setBirthday(LocalDate.of(1946, 8, 20));

        User createdUser = userController.create(user);

        assertNotNull(createdUser.getId());
        assertEquals("mail@mail.ru", createdUser.getEmail());
        assertEquals("dolore", createdUser.getLogin());
        assertEquals("Nick Name", createdUser.getName());
        assertEquals(LocalDate.of(1946, 8, 20), createdUser.getBirthday());
        assertEquals(1, userController.findAll().size());
    }

    @Test
    void shouldSetLoginAsNameWhenNameIsBlank() {
        User user = new User();
        user.setEmail("mail@mail.ru");
        user.setLogin("dolore");
        user.setName("");
        user.setBirthday(LocalDate.of(1946, 8, 20));

        User createdUser = userController.create(user);

        assertEquals("dolore", createdUser.getName());
    }

    @Test
    void shouldThrowExceptionWhenEmailIsNull() {
        User user = new User();
        user.setEmail(null);
        user.setLogin("dolore");
        user.setName("Nick Name");
        user.setBirthday(LocalDate.of(1946, 8, 20));

        assertThrows(ValidationException.class, () -> userController.create(user));
    }

    @Test
    void shouldThrowExceptionWhenEmailIsBlank() {
        User user = new User();
        user.setEmail("");
        user.setLogin("dolore");
        user.setName("Nick Name");
        user.setBirthday(LocalDate.of(1946, 8, 20));

        assertThrows(ValidationException.class, () -> userController.create(user));
    }

    @Test
    void shouldThrowExceptionWhenEmailHasNoAtSign() {
        User user = new User();
        user.setEmail("mailmail.ru");
        user.setLogin("dolore");
        user.setName("Nick Name");
        user.setBirthday(LocalDate.of(1946, 8, 20));

        assertThrows(ValidationException.class, () -> userController.create(user));
    }

    @Test
    void shouldThrowExceptionWhenLoginIsBlank() {
        User user = new User();
        user.setEmail("mail@mail.ru");
        user.setLogin("");
        user.setName("Nick Name");
        user.setBirthday(LocalDate.of(1946, 8, 20));

        assertThrows(ValidationException.class, () -> userController.create(user));
    }

    @Test
    void shouldThrowExceptionWhenLoginContainsSpaces() {
        User user = new User();
        user.setEmail("mail@mail.ru");
        user.setLogin("my login");
        user.setName("Nick Name");
        user.setBirthday(LocalDate.of(1946, 8, 20));

        assertThrows(ValidationException.class, () -> userController.create(user));
    }

    @Test
    void shouldThrowExceptionWhenBirthdayIsInFuture() {
        User user = new User();
        user.setEmail("mail@mail.ru");
        user.setLogin("dolore");
        user.setName("Nick Name");
        user.setBirthday(LocalDate.now().plusDays(1));

        assertThrows(ValidationException.class, () -> userController.create(user));
    }

    @Test
    void shouldCreateUserWhenBirthdayIsToday() {
        User user = new User();
        user.setEmail("mail@mail.ru");
        user.setLogin("dolore");
        user.setName("Nick Name");
        user.setBirthday(LocalDate.now());

        User createdUser = userController.create(user);

        assertNotNull(createdUser.getId());
        assertEquals(LocalDate.now(), createdUser.getBirthday());
    }

    @Test
    void shouldUpdateExistingUser() {
        User user = new User();
        user.setEmail("mail@mail.ru");
        user.setLogin("dolore");
        user.setName("Nick Name");
        user.setBirthday(LocalDate.of(1946, 8, 20));

        User createdUser = userController.create(user);

        User updatedUser = new User();
        updatedUser.setId(createdUser.getId());
        updatedUser.setEmail("new@mail.ru");
        updatedUser.setLogin("newlogin");
        updatedUser.setName("New Name");
        updatedUser.setBirthday(LocalDate.of(2000, 1, 1));

        User result = userController.update(updatedUser);

        assertEquals(createdUser.getId(), result.getId());
        assertEquals("new@mail.ru", result.getEmail());
        assertEquals("newlogin", result.getLogin());
        assertEquals("New Name", result.getName());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingUserWithoutId() {
        User user = new User();
        user.setEmail("mail@mail.ru");
        user.setLogin("dolore");
        user.setName("Nick Name");
        user.setBirthday(LocalDate.of(1946, 8, 20));

        assertThrows(ValidationException.class, () -> userController.update(user));
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistingUser() {
        User user = new User();
        user.setId(999L);
        user.setEmail("mail@mail.ru");
        user.setLogin("dolore");
        user.setName("Nick Name");
        user.setBirthday(LocalDate.of(1946, 8, 20));

        assertThrows(ValidationException.class, () -> userController.update(user));
    }

    @Test
    void shouldDeleteExistingUser() {
        User user = new User();
        user.setEmail("mail@mail.ru");
        user.setLogin("dolore");
        user.setName("Nick Name");
        user.setBirthday(LocalDate.of(1946, 8, 20));

        User createdUser = userController.create(user);
        userController.delete(createdUser.getId());

        assertEquals(0, userController.findAll().size());
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistingUser() {
        assertThrows(ValidationException.class, () -> userController.delete(999L));
    }

}