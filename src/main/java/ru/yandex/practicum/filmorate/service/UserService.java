package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserStorage userStorage;

    public User addFriend(long userId, long friendId) {
        User user = getUserOrThrow(userId);
        User friend = getUserOrThrow(friendId);

        user.getFriends().add(friendId);
        friend.getFriends().add(userId);

        userStorage.update(user);
        userStorage.update(friend);
        return user;
    }

    public User removeFriend(long userId, long friendId) {
        User user = getUserOrThrow(userId);
        User friend = getUserOrThrow(friendId);

        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);

        userStorage.update(user);
        userStorage.update(friend);
        return user;
    }

    public Collection<User> getFriends(long userId) {
        User user = getUserOrThrow(userId);
        return user.getFriends().stream()
                .map(this::getUserOrThrow)
                .collect(Collectors.toList());
    }

    public Collection<User> getCommonFriends(long userId, long otherId) {
        User user = getUserOrThrow(userId);
        User other = getUserOrThrow(otherId);

        return user.getFriends().stream()
                .filter(other.getFriends()::contains)
                .map(this::getUserOrThrow)
                .collect(Collectors.toList());
    }

    private User getUserOrThrow(long id) {
        return userStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
    }
}
