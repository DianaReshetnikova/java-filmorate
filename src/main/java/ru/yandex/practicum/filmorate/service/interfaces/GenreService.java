package ru.yandex.practicum.filmorate.service.interfaces;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;

public interface GenreService {
    Collection<Genre> getGenres();

    Genre getGenreById(Integer id);
}
