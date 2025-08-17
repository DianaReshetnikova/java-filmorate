package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {
    public Collection<Film> getFilms();

    public Film createFilm(Film newFilm);

    public Film updateFilm(Film newFilm);

    public void deleteFilm(Long id);
    public Film getFilmById(Long id);
}
