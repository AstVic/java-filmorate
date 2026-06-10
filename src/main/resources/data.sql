INSERT INTO users (login, name, email, birthday)
VALUES
    ('vika', 'Виктория', 'vika@mail.ru', '2004-05-12'),
    ('nastya', 'Анастасия', 'nastya@mail.ru', '2003-11-03'),
    ('alex', 'Алексей', 'alex@mail.ru', '2002-07-21'),
    ('maria', 'Мария', 'maria@mail.ru', '2005-01-18'),
    ('ivan', 'Иван', 'ivan@mail.ru', '2001-09-30'),
    ('dima', 'Дмитрий', 'dima@mail.ru', '2004-03-06');


INSERT INTO mpa (rating, description)
VALUES
    ('G', 'У фильма нет возрастных ограничений'),
    ('PG', 'Детям рекомендуется смотреть фильм с родителями'),
    ('PG-13', 'Детям до 13 лет просмотр не желателен'),
    ('R', 'Лицам до 17 лет просмотр разрешён только в присутствии взрослого'),
    ('NC-17', 'Лицам до 18 лет просмотр запрещён');


INSERT INTO genres (name)
VALUES
    ('Комедия'),
    ('Драма'),
    ('Мультфильм'),
    ('Триллер'),
    ('Документальный'),
    ('Боевик');


INSERT INTO films (name, description, release_date, duration, mpa_id)
VALUES
    ('Интерстеллар', 'Фантастический фильм о космосе, времени и семье', '2014-11-06', 169,
        (SELECT id FROM mpa WHERE rating = 'PG-13')),

    ('Зелёная книга', 'История дружбы музыканта и водителя во время турне', '2018-09-11', 130,
        (SELECT id FROM mpa WHERE rating = 'PG-13')),

    ('Король Лев', 'Мультфильм о взрослении львёнка Симбы', '1994-06-24', 88,
        (SELECT id FROM mpa WHERE rating = 'G')),

    ('Начало', 'Фильм о проникновении в сны и управлении сознанием', '2010-07-16', 148,
        (SELECT id FROM mpa WHERE rating = 'PG-13')),

    ('1+1', 'Драма и комедия о дружбе аристократа и его помощника', '2011-11-02', 112,
        (SELECT id FROM mpa WHERE rating = 'R')),

    ('Остров проклятых', 'Психологический триллер о расследовании в клинике', '2010-02-13', 138,
        (SELECT id FROM mpa WHERE rating = 'R')),

    ('Земля: Один потрясающий день', 'Документальный фильм о природе и животных', '2017-08-04', 95,
        (SELECT id FROM mpa WHERE rating = 'G'));


INSERT INTO likes (user_id, film_id)
VALUES
    ((SELECT id FROM users WHERE login = 'vika'),   (SELECT id FROM films WHERE name = 'Интерстеллар')),
    ((SELECT id FROM users WHERE login = 'vika'),   (SELECT id FROM films WHERE name = 'Начало')),
    ((SELECT id FROM users WHERE login = 'nastya'), (SELECT id FROM films WHERE name = 'Зелёная книга')),
    ((SELECT id FROM users WHERE login = 'nastya'), (SELECT id FROM films WHERE name = '1+1')),
    ((SELECT id FROM users WHERE login = 'alex'),   (SELECT id FROM films WHERE name = 'Интерстеллар')),
    ((SELECT id FROM users WHERE login = 'maria'),  (SELECT id FROM films WHERE name = 'Король Лев')),
    ((SELECT id FROM users WHERE login = 'ivan'),   (SELECT id FROM films WHERE name = 'Остров проклятых'));


INSERT INTO film_genres (film_id, genre_id)
VALUES
    ((SELECT id FROM films WHERE name = 'Интерстеллар'), (SELECT id FROM genres WHERE name = 'Драма')),
    ((SELECT id FROM films WHERE name = 'Интерстеллар'), (SELECT id FROM genres WHERE name = 'Документальный')),
    ((SELECT id FROM films WHERE name = 'Зелёная книга'), (SELECT id FROM genres WHERE name = 'Драма')),
    ((SELECT id FROM films WHERE name = 'Король Лев'), (SELECT id FROM genres WHERE name = 'Мультфильм')),
    ((SELECT id FROM films WHERE name = 'Начало'), (SELECT id FROM genres WHERE name = 'Триллер')),
    ((SELECT id FROM films WHERE name = '1+1'), (SELECT id FROM genres WHERE name = 'Комедия')),
    ((SELECT id FROM films WHERE name = 'Остров проклятых'), (SELECT id FROM genres WHERE name = 'Триллер'));


INSERT INTO friendships (user_id, friend_id, status)
VALUES
    ((SELECT id FROM users WHERE login = 'vika'),   (SELECT id FROM users WHERE login = 'nastya'), 'CONFIRMED'),
    ((SELECT id FROM users WHERE login = 'vika'),   (SELECT id FROM users WHERE login = 'alex'), 'PENDING'),
    ((SELECT id FROM users WHERE login = 'nastya'), (SELECT id FROM users WHERE login = 'vika'), 'CONFIRMED'),
    ((SELECT id FROM users WHERE login = 'nastya'), (SELECT id FROM users WHERE login = 'maria'), 'PENDING'),
    ((SELECT id FROM users WHERE login = 'alex'),   (SELECT id FROM users WHERE login = 'ivan'), 'CONFIRMED'),
    ((SELECT id FROM users WHERE login = 'maria'),  (SELECT id FROM users WHERE login = 'dima'), 'PENDING'),
    ((SELECT id FROM users WHERE login = 'ivan'),   (SELECT id FROM users WHERE login = 'vika'), 'CONFIRMED');