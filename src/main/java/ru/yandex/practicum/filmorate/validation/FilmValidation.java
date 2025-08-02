package ru.yandex.practicum.filmorate.validation;

import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.time.Month;

public class FilmValidation {
    public static void validateFilm(Film newFilm) throws ValidationException {
        if (newFilm.getReleaseDate().isBefore(LocalDate.of(1895, Month.DECEMBER, 28)))
            throw new ValidationException("Дата релиза должна быть не раньше 28 декабря 1895 года;");
    }
}
