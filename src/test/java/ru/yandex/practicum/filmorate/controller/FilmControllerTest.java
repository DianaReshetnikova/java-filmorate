package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FilmControllerTest {
    private final FilmController filmController = new FilmController();

    //                              should not create film
    @Test
    void shouldNotCreateFilmWithIncorrectReleaseDate() {
        Film film = Film.builder()
                .name("Shrek")
                .description("Some description")
                .releaseDate(LocalDate.of(1890, Month.JANUARY, 1))
                .duration(120)
                .build();

        ValidationException exception = assertThrows(ValidationException.class, () ->
                filmController.createFilm(film));
        assertNotNull(exception, "Дата релиза — не раньше 28 декабря 1895 года");
        assertEquals(0, filmController.getFilms().stream().toList().size(), "Коллекция фильмов должна содержать 0 элементов");
    }

    //                                  should create film
    @Test
    void shouldCreateFilmWithBoundaryReleaseDate() {
        Film film = Film.builder()
                .name("Shrek")
                .description("Some description")
                .releaseDate(LocalDate.of(1895, Month.DECEMBER, 28))
                .duration(120)
                .build();

        assertDoesNotThrow(() -> filmController.createFilm(film), "Фильм с граничной датой должен быть создан.");

        assertEquals(1, filmController.getFilms().stream().toList().size(), "Коллекция фильмов должна содержать 1 элемент");
        Film createdFilm = filmController.getFilms().stream().toList().getFirst();
        assertTrue(equalsFilmFieldsWithoutId(film, createdFilm), "Поля фильмов должны совпадать");
    }


    @Test
    void shouldCreateFilmWithCurrentReleaseDate() {
        Film film = Film.builder()
                .name("Shrek")
                .description("Some description")
                .releaseDate(LocalDate.now())
                .duration(120)
                .build();

        assertDoesNotThrow(() -> filmController.createFilm(film), "Фильм с текущей датой должен быть создан.");

        assertEquals(1, filmController.getFilms().stream().toList().size(), "Коллекция фильмов должна содержать 1 элемент");
        Film createdFilm = filmController.getFilms().stream().toList().getFirst();
        assertTrue(equalsFilmFieldsWithoutId(film, createdFilm), "Поля фильмов должны совпадать");
    }

    @Test
    void shouldCreateFilmWithDescriptionLength200() {
        Film film = Film.builder()
                .name("Shrek")
                .description(String.valueOf('s').repeat(200))
                .releaseDate(LocalDate.now())
                .duration(120)
                .build();

        assertDoesNotThrow(() -> filmController.createFilm(film), "Фильм с описанием в 200 символов должен быть создан.");

        assertEquals(1, filmController.getFilms().stream().toList().size(), "Коллекция фильмов должна содержать 1 элемент");
        Film createdFilm = filmController.getFilms().stream().toList().getFirst();
        assertTrue(equalsFilmFieldsWithoutId(film, createdFilm), "Поля фильмов должны совпадать");
    }

    @Test
    void shouldCreateFilmWithZeroDuration() {
        Film film = Film.builder()
                .name("Shrek")
                .description("Some description")
                .releaseDate(LocalDate.now())
                .duration(0)
                .build();

        assertDoesNotThrow(() -> filmController.createFilm(film), "Фильм с нулевой длительностью должен быть создан.");

        assertEquals(1, filmController.getFilms().stream().toList().size(), "Коллекция фильмов должна содержать 1 элемент");
        Film createdFilm = filmController.getFilms().stream().toList().getFirst();
        assertTrue(equalsFilmFieldsWithoutId(film, createdFilm), "Поля фильмов должны совпадать");
    }

    @Test
    void shouldCreateFilmWithPositiveDuration() {
        Film film = Film.builder()
                .name("Shrek")
                .description("Some description")
                .releaseDate(LocalDate.now())
                .duration(200)
                .build();

        assertDoesNotThrow(() -> filmController.createFilm(film), "Фильм с положительной длительностью должен быть создан.");

        assertEquals(1, filmController.getFilms().stream().toList().size(), "Коллекция фильмов должна содержать 1 элемент");
        Film createdFilm = filmController.getFilms().stream().toList().getFirst();
        assertTrue(equalsFilmFieldsWithoutId(film, createdFilm), "Поля фильмов должны совпадать");
    }

    @Test
    void shouldCreateFilmWithCorrectName() {
        Film film = Film.builder()
                .name("Shrek")
                .description("Some description")
                .releaseDate(LocalDate.now())
                .duration(200)
                .build();

        assertDoesNotThrow(() -> filmController.createFilm(film), "Фильм с заполненным названием должен быть создан.");

        assertEquals(1, filmController.getFilms().stream().toList().size(), "Коллекция фильмов должна содержать 1 элемент");
        Film createdFilm = filmController.getFilms().stream().toList().getFirst();
        assertTrue(equalsFilmFieldsWithoutId(film, createdFilm), "Поля фильмов должны совпадать");
    }

    @Test
    void get2FilmsWithCorrectData() {
        Film filmShrek1 = Film.builder()
                .name("Shrek 1")
                .description("First film about ogre Shrek and princess Fiona")
                .releaseDate(LocalDate.now())
                .duration(120)
                .build();

        Film filmShrek2 = Film.builder()
                .name("Shrek 2")
                .description("Second film about ogre Shrek and princess Fiona")
                .releaseDate(LocalDate.now())
                .duration(200)
                .build();

        filmController.createFilm(filmShrek1);
        filmController.createFilm(filmShrek2);

        assertEquals(2, filmController.getFilms().stream().toList().size(), "Коллекция фильмов должна содержать 2 элемента");

        Film createdFilmShrek1 = filmController.getFilms().stream().toList().getFirst();
        Film createdFilmShrek2 = filmController.getFilms().stream().toList().getLast();

        assertTrue(equalsFilmFieldsWithoutId(filmShrek1, createdFilmShrek1), "Поля первого фильма не совпадают с извлеченным из списка");
        assertTrue(equalsFilmFieldsWithoutId(filmShrek2, createdFilmShrek2), "Поля второго фильма не совпадают с извлеченным из списка");
    }

    //                                  should update film
    @Test
    void shouldUpdateFilm() {
        Film film = Film.builder()
                .name("Film")
                .description("Some description")
                .releaseDate(LocalDate.now())
                .duration(120)
                .build();

        filmController.createFilm(film);
        Film createdFilm = filmController.getFilms().stream().toList().getFirst();

        assertDoesNotThrow(() -> filmController.updateFilm(film.toBuilder()
                .description("Updated some description")
                .build()), "Фильм должен быть обновлен.");

        Film updatedFilm = filmController.getFilms().stream().toList().getFirst();
        boolean isEquals = createdFilm.getName().equals(updatedFilm.getName()) &&
                createdFilm.getReleaseDate().equals(updatedFilm.getReleaseDate()) &&
                createdFilm.getDuration() == updatedFilm.getDuration() &&
                updatedFilm.getDescription().equals("Updated some description");

        assertTrue(isEquals);
    }

    //                                  should not update film
    @Test
    void shouldNotUpdateFilmWithNullId() {
        Film film = Film.builder()
                .name("Film")
                .description("Some description")
                .releaseDate(LocalDate.now())
                .duration(120)
                .build();

        filmController.createFilm(film);

        ValidationException exception = assertThrows(ValidationException.class, () ->
                filmController.updateFilm(film.toBuilder()
                        .id(null)
                        .description("Updated some description")
                        .build()));
        assertNotNull(exception, "Фильм с переданным значением id = null не должен быть обновлен.");
    }

    @Test
    void shouldNotUpdateFilmWithNotExistingId() {
        NotFoundException exception = assertThrows(NotFoundException.class, () ->
                filmController.updateFilm(Film.builder()
                        .id(888888888L)
                        .description("Updated some description")
                        .build()));
        assertNotNull(exception, "Фильм с несуществующим значением id не должен быть обновлен.");
    }

    private boolean equalsFilmFieldsWithoutId(Film film1, Film film2) {
        return film1.getName().equals(film2.getName()) &&
                film1.getDescription().equals(film2.getDescription()) &&
                film1.getReleaseDate().equals(film2.getReleaseDate()) &&
                film1.getDuration() == (film2.getDuration());
    }
}