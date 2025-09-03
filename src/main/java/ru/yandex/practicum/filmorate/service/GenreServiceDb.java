package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.interfaces.GenreService;
import ru.yandex.practicum.filmorate.storage.interfaces.GenreStorage;

import java.util.Collection;

// Указываем, что класс является бином и его
// нужно добавить в контекст приложения
@Service
@Slf4j
@RequiredArgsConstructor
public class GenreServiceDb implements GenreService {
    private final GenreStorage genreStorage;

    @Override
    public Collection<Genre> getGenres() {
        return genreStorage.getGenres();
    }

    @Override
    public Genre getGenreById(Integer id) {
        return genreStorage.getGenreById(id).orElseThrow(() -> new NotFoundException("Жанр с id = \" + id + \" не найден"));
    }
}
