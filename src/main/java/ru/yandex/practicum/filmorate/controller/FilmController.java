package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.validation.FilmValidation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private final Map<Long, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> getFilms() {
        return films.values();
    }

    @PostMapping
    public Film createFilm(@Validated @RequestBody Film newFilm) {
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

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film newFilm) {
        try {
            if (newFilm.getId() == null)
                throw new ValidationException("Id фильма должен быть указан");
            if (!films.containsKey(newFilm.getId()))
                throw new NotFoundException("Фильм с id = " + newFilm.getId() + " не найден");

            FilmValidation.validateFilm(newFilm);
            films.put(newFilm.getId(), newFilm);
            log.info("Обновлен фильм: {}.", newFilm);
            return newFilm;
        } catch (ValidationException | NotFoundException ex) {
            log.debug(ex.getMessage());
            throw ex;
        }
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
