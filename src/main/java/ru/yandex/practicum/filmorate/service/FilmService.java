package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;

// Указываем, что класс является бином и его
// нужно добавить в контекст приложения
@Service
@Slf4j
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public Film addLikeToFilm(Long filmId, Long userId) {
        Film film = filmStorage.getFilmById(filmId);
        User user = userStorage.getUserById(userId);

        if (film != null && user != null) {
            Set<Long> userIdsLikes = film.getUserIdsLiked();
            if (!userIdsLikes.contains(userId)) {
                userIdsLikes.add(userId);
                film.setUserIdsLiked(userIdsLikes);
                log.info("Пользователь {} поставил лайк фильму: {}.", userId, filmId);
            }
        }

        return film;
    }

    public Film deleteLikeFromFilm(Long filmId, Long userId) {
        Film film = filmStorage.getFilmById(filmId);
        User user = userStorage.getUserById(userId);

        if (film != null && user != null) {
            Set<Long> userIdsLikes = film.getUserIdsLiked();
            if(userIdsLikes.contains(userId)){
                userIdsLikes.remove(userId);
                film.setUserIdsLiked(userIdsLikes);
                log.info("Пользователь {} удалил лайк с фильма: {}.", userId, filmId);
            }
        }

        return film;
    }

    //вернуть коллекцию фильмов с сортировкой по убыванию по количеству лайков
    public Collection<Film> getTopPopularFilms(Integer count) {
        return filmStorage.getFilms().stream()
                .sorted(Comparator.comparing(film -> film.getUserIdsLiked().size()))
                .limit(count)
                .collect(Collectors.toList());
    }
}
