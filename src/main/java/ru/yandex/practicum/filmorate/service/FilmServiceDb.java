package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.InvalidJsonFieldException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.service.interfaces.FilmService;
import ru.yandex.practicum.filmorate.storage.interfaces.FilmStorage;
import ru.yandex.practicum.filmorate.storage.interfaces.GenreStorage;
import ru.yandex.practicum.filmorate.storage.interfaces.MpaStorage;
import ru.yandex.practicum.filmorate.storage.interfaces.UserStorage;
import ru.yandex.practicum.filmorate.validation.FilmValidation;

import java.util.Collection;
import java.util.Set;

// Указываем, что класс является бином и его
// нужно добавить в контекст приложения
@Service
@Slf4j
@RequiredArgsConstructor
public class FilmServiceDb implements FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final MpaStorage mpaStorage;
    private final GenreStorage genreStorage;

    @Override
    public Collection<Film> getFilms() {
        return filmStorage.getFilms();
    }

    @Override
    public Film createFilm(Film newFilm) {
        try {
            if (newFilm.getId() != null)
                throw new InvalidJsonFieldException("Для нового фильма нельзя указать Id");

            FilmValidation.validateFilmReleaseDate(newFilm);
            validateMpa(newFilm.getMpa());
            validateGenres(newFilm.getGenres());

            return filmStorage.createFilm(newFilm);
        } catch (ValidationException | InvalidJsonFieldException | NotFoundException ex) {
            log.debug(ex.getMessage());
            throw ex;
        }
    }

    @Override
    public Film updateFilm(Film newFilm) {
        try {
            validateFilmId(newFilm.getId());
            FilmValidation.validateFilmReleaseDate(newFilm);
            return filmStorage.updateFilm(newFilm);
        } catch (ValidationException | NotFoundException ex) {
            log.debug(ex.getMessage());
            throw ex;
        }
    }

    @Override
    public void deleteFilm(Long id) {
        try {
            validateFilmId(id);
            filmStorage.deleteFilm(id);
        } catch (ValidationException | NotFoundException ex) {
            log.debug(ex.getMessage());
            throw ex;
        }
    }

    @Override
    public Film getFilmById(Long id) {
        try {
            validateFilmId(id);
            return filmStorage.getFilmById(id).get();
        } catch (ValidationException | NotFoundException ex) {
            log.debug(ex.getMessage());
            throw ex;
        }
    }

    @Override
    public void addLikeToFilm(Long filmId, Long userId) {
        try {
            validateFilmId(filmId);
            validateUserIdExists(userId);

            filmStorage.addLikeToFilm(filmId, userId);
        } catch (ValidationException | NotFoundException ex) {
            log.debug(ex.getMessage());
            throw ex;
        }
    }

    @Override
    public void deleteLikeFromFilm(Long filmId, Long userId) {
        try {
            validateFilmId(filmId);
            validateUserIdExists(userId);

            filmStorage.removeLikeFromFilm(filmId, userId);
        } catch (ValidationException | NotFoundException ex) {
            log.debug(ex.getMessage());
            throw ex;
        }
    }

    //вернуть коллекцию фильмов с сортировкой по убыванию по количеству лайков
    @Override
    public Collection<Film> getTopPopularFilms(Integer count) {
        try {
            if (count <= 0)
                throw new IllegalArgumentException("Параметр count должен быть положительным числом");
            return filmStorage.getTopPopularFilms(count);
        } catch (IllegalArgumentException ex) {
            log.debug(ex.getMessage());
            throw ex;
        }
    }


    private void validateMpa(MPA mpa) throws NotFoundException {
        if (mpa != null) {
            var result = mpaStorage.getMpaById(mpa.getId());
            if (result.isEmpty())
                throw new NotFoundException("Возрастной рейтинг с id = " + mpa.getId() + " не найден");
        }
    }

    private void validateGenres(Set<Genre> genres) throws NotFoundException {
        if (genres != null) {
            for (var genre : genres) {
                var result = genreStorage.getGenreById(genre.getId());
                if (result.isEmpty())
                    throw new NotFoundException("Жанр с id = " + genre.getId() + " не найден");
            }
        }
    }

    private void validateFilmId(Long id) throws NotFoundException, ValidationException {
        if (id == null)
            throw new ValidationException("Id фильма должен быть указан");
        if (filmStorage.getFilmById(id).isEmpty())
            throw new NotFoundException("Фильм с id = " + id + " не найден");
    }

    private void validateUserIdExists(Long id) throws NotFoundException {
        if (id == null)
            throw new ValidationException("Id пользователя должен быть указан");
        if (userStorage.getUserById(id).isEmpty())
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
    }
}
