package ru.yandex.practicum.filmorate.storage.interfaces;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Optional;

public interface FilmStorage {
    Collection<Film> getFilms();

    Film createFilm(Film newFilm);

    Film updateFilm(Film newFilm);

    void deleteFilm(Long id);

    Optional<Film> getFilmById(Long id);

    void addLikeToFilm(Long filmId, Long userId);

    void removeLikeFromFilm(Long filmId, Long userId);

    Collection<Film> getTopPopularFilms(Integer count);

    boolean isLikeAlreadyExist(Long filmId, Long userId);

    void deleteAll();
}