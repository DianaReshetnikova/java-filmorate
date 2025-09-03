package ru.yandex.practicum.filmorate.storage.interfaces;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.Optional;

public interface GenreStorage {
    public Collection<Genre> getGenres();

    public Optional<Genre> getGenreById(Integer id);
}
