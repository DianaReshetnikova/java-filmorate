package ru.yandex.practicum.filmorate.storage;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.validation.FilmValidation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();

    @Override
    public Collection<Film> getFilms() {
        return films.values();
    }

    @Override
    public Film createFilm(Film newFilm) {
        try {
            FilmValidation.validateFilm(newFilm);
            newFilm.setId(getNextId());
            films.put(newFilm.getId(), newFilm);
            log.info("Добавлен фильм: {}.", newFilm);
            return newFilm;
        } catch (ValidationException ex) {
            log.debug(ex.getMessage());
            throw ex;
        }
    }

    @Override
    public Film updateFilm(Film newFilm) {
        try {
            validateFilmId(newFilm.getId());
            FilmValidation.validateFilm(newFilm);
            films.put(newFilm.getId(), newFilm);
            log.info("Обновлен фильм: {}.", newFilm);
            return newFilm;
        } catch (ValidationException | NotFoundException ex) {
            log.debug(ex.getMessage());
            throw ex;
        }
    }

    @Override
    public void deleteFilm(Long id) {
        try {
            validateFilmId(id);
            films.remove(id);
            log.info("Удален фильм: {}.", id);
        } catch (ValidationException | NotFoundException ex) {
            log.debug(ex.getMessage());
            throw ex;
        }
    }

    @Override
    public Film getFilmById(Long id) {
        try {
            validateFilmId(id);
            log.info("Запрошен фильм: {}.", id);
            return films.get(id);
        } catch (ValidationException | NotFoundException ex) {
            log.debug(ex.getMessage());
            throw ex;
        }
    }


    private void validateFilmId(Long id) {
        if (id == null)
            throw new ValidationException("Id фильма должен быть указан");
        if (!films.containsKey(id))
            throw new NotFoundException("Фильм с id = " + id + " не найден");
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
