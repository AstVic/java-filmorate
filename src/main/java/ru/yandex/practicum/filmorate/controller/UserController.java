package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private Map<Long, User> users = new HashMap<>();

    public void check(User user) throws ValidationException {
        if (user.getEmail() == null || user.getEmail().isEmpty() || !user.getEmail().contains("@")) {
            log.error("Ошибка валидации: email некорректен - {}", user.getEmail());
            throw new ValidationException("Почта не может быть пустой и обязана содержать символ '@'");
        }
        if (user.getLogin() == null || user.getLogin().isEmpty() || user.getLogin().contains(" ")) {
            log.error("Ошибка валидации: логин некорректен - '{}'", user.getLogin());
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        }
        if (user.getBirthday() == null || user.getBirthday().isAfter(Instant.now())) {
            log.error("Ошибка валидации: дата рождения некорректна - {}", user.getBirthday());
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }

    @PostMapping
    public User create(@RequestBody User user) throws ValidationException {
        log.info("Получен запрос на создание пользователя: {}", user);
        check(user);
        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("Пользователь успешно создан с id {}: {}", user.getId(), user);
        return user;
    }

    @PutMapping
    public User update(@RequestBody User newUser) throws ValidationException {
        log.info("Получен запрос на обновление пользователя с id {}: {}", newUser.getId(), newUser);
        if (newUser.getId() == null) {
            log.error("Ошибка обновления: id пользователя не указан");
            throw new ValidationException("Id должен быть указан");
        }
        if (!users.containsKey(newUser.getId())) {
            log.error("Ошибка обновления: пользователь с id {} не найден", newUser.getId());
            throw new ValidationException("Пользователь не найден");
        }

        User user = users.get(newUser.getId());

        if (newUser.getName() != null) {
            user.setName(newUser.getName());
        } else {
            user.setName(newUser.getLogin());
        }
        if (newUser.getEmail() != null) {
            user.setEmail(newUser.getEmail());
        }
        if (newUser.getBirthday() != null) {
            user.setBirthday(newUser.getBirthday());
        }
        if (newUser.getLogin() != null) {
            user.setLogin(newUser.getLogin());
        }

        check(user);
        log.info("Пользователь с id {} успешно обновлен: {}", user.getId(), user);
        return user;
    }

    @GetMapping
    public Collection<User> findAll() {
        log.info("Получен запрос на получение всех пользователей");
        Collection<User> allUsers = users.values();
        log.info("Возвращено {} пользователей", allUsers.size());
        return allUsers;
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}