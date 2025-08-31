package ru.yandex.practicum.filmorate.storage.interfaces;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Optional;

public interface FilmStorage {
    public Collection<Film> getFilms();

    public Film createFilm(Film newFilm);

    public Film updateFilm(Film newFilm);

    public void deleteFilm(Long id);

    public Optional<Film> getFilmById(Long id);

    public void addLikeToFilm(Long filmId, Long userId);

    public void removeLikeFromFilm(Long filmId, Long userId);

    public Collection<Film> getTopPopularFilms(Integer count);
}