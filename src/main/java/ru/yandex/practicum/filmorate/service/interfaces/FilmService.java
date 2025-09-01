package ru.yandex.practicum.filmorate.service.interfaces;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmService {
    Collection<Film> getFilms();

    Film createFilm(Film newFilm);

    Film updateFilm(Film newFilm);

    void deleteFilm(Long id);

    Film getFilmById(Long id);

    void addLikeToFilm(Long filmId, Long userId);

    void deleteLikeFromFilm(Long filmId, Long userId);

    Collection<Film> getTopPopularFilms(Integer count);
}
