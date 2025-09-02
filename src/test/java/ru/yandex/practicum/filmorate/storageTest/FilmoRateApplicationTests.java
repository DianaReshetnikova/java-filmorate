package ru.yandex.practicum.filmorate.storageTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.FilmorateApplication;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.interfaces.FilmService;
import ru.yandex.practicum.filmorate.service.interfaces.UserService;
import ru.yandex.practicum.filmorate.storage.dao.MpaDbStorage;
import ru.yandex.practicum.filmorate.storage.interfaces.FilmStorage;
import ru.yandex.practicum.filmorate.storage.interfaces.GenreStorage;
import ru.yandex.practicum.filmorate.storage.interfaces.UserStorage;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = FilmorateApplication.class)
@AutoConfigureTestDatabase
class FilmoRateApplicationTests {
    @Autowired
    private FilmService filmService;
    @Autowired
    private FilmStorage filmStorage;
    @Autowired
    private UserService userService;
    @Autowired
    private UserStorage userStorage;
    @Autowired
    private MpaDbStorage mpaStorage;
    @Autowired
    private GenreStorage genreStorage;

    @BeforeEach
    void beforeEach() {
        filmStorage.deleteAll();
        userStorage.deleteAll();
    }

    @Test
    public void createFilm() {
        Set<Genre> genresSet = new HashSet<>();
        genresSet.add(genreStorage.getGenreById(1).get());

        Film film = Film.builder()
                .name("Titanic")
                .description("descr")
                .releaseDate(LocalDate.of(2001, 01, 01))
                .duration(200)
                .mpa(mpaStorage.getMpaById(1).get())
                .userIdsLiked(new HashSet<>())
                .genres(genresSet)
                .build();

        Film created = filmService.createFilm(film);
        assertNotEquals(0, created.getId());

        Film repo = filmStorage.getFilmById(created.getId()).orElseThrow();
        assertEquals(film.getName(), repo.getName());
    }

    @Test
    public void getFilms() {
        Film film = Film.builder()
                .name("Titanic")
                .description("descr")
                .releaseDate(LocalDate.of(2001, 01, 01))
                .duration(200)
                .userIdsLiked(new HashSet<>())
                .genres(new HashSet<>())
                .build();

        Film film2 = film.toBuilder()
                .name("Gone with the wind")
                .description("description")
                .releaseDate(LocalDate.of(1988, 01, 01))
                .duration(300)
                .build();

        Film filmCreated = filmService.createFilm(film);
        Film film2Created = filmService.createFilm(film2);

        List<Film> repo = filmStorage.getFilms().stream().toList();
        assertEquals(filmCreated.getId(), repo.get(0).getId());
        assertEquals(film2Created.getId(), repo.get(1).getId());
    }

    @Test
    public void updateFilm() {
        Film film = Film.builder()
                .name("Titanic")
                .description("descr")
                .releaseDate(LocalDate.of(2001, 01, 01))
                .duration(200)
                .userIdsLiked(new HashSet<>())
                .genres(new HashSet<>())
                .build();

        Film filmCreated = filmService.createFilm(film);

        Film update = film.toBuilder().name("new name").build();
        Film filmUpdated = filmService.updateFilm(update);

        Film repo = filmStorage.getFilmById(filmCreated.getId()).get();
        assertEquals(filmCreated.getId(), filmUpdated.getId());
        assertEquals(update.getName(), filmUpdated.getName());
    }

    @Test
    public void deleteFilm() {
        Film film = Film.builder()
                .name("Titanic")
                .description("descr")
                .releaseDate(LocalDate.of(2001, 01, 01))
                .duration(200)
                .userIdsLiked(new HashSet<>())
                .genres(new HashSet<>())
                .build();

        Film filmCreated = filmService.createFilm(film);
        Film repo = filmStorage.getFilmById(filmCreated.getId()).get();
        assertEquals(filmCreated.getId(), repo.getId());

        filmStorage.deleteFilm(filmCreated.getId());

        Optional<Film> opt = filmStorage.getFilmById(filmCreated.getId());
        assertEquals(Optional.empty(), opt);
    }

    @Test
    public void addLikeToFilm() {
        Film film = Film.builder()
                .name("Titanic")
                .description("descr")
                .releaseDate(LocalDate.of(2001, 01, 01))
                .duration(200)
                .genres(new HashSet<>())
                .userIdsLiked(new HashSet<>())
                .build();

        Film filmCreated = filmService.createFilm(film);
        Film repo = filmStorage.getFilmById(filmCreated.getId()).get();
        assertEquals(filmCreated.getId(), repo.getId());

        User user = User.builder()
                .name("user")
                .birthday(LocalDate.now())
                .email("user@yandex.ru")
                .login("loginUser")
                .friendsIds(new HashSet<>())
                .build();
        User createdUser = userService.createUser(user);

        filmService.addLikeToFilm(filmCreated.getId(), createdUser.getId());

        assertTrue(filmStorage.isLikeAlreadyExist(filmCreated.getId(), createdUser.getId()));
    }

    @Test
    public void deleteLikeFromFilm() {
        Film film = Film.builder()
                .name("Titanic")
                .description("descr")
                .releaseDate(LocalDate.of(2001, 01, 01))
                .duration(200)
                .userIdsLiked(new HashSet<>())
                .genres(new HashSet<>())
                .build();

        Film filmCreated = filmService.createFilm(film);
        Film repo = filmStorage.getFilmById(filmCreated.getId()).get();
        assertEquals(filmCreated.getId(), repo.getId());

        User user = User.builder()
                .name("user")
                .birthday(LocalDate.now())
                .email("user@yandex.ru")
                .login("loginUser")
                .friendsIds(new HashSet<>())
                .build();
        User createdUser = userService.createUser(user);

        filmService.addLikeToFilm(filmCreated.getId(), createdUser.getId());

        assertTrue(filmStorage.isLikeAlreadyExist(filmCreated.getId(), createdUser.getId()));

        filmService.deleteLikeFromFilm(filmCreated.getId(), createdUser.getId());
        assertFalse(filmStorage.isLikeAlreadyExist(filmCreated.getId(), createdUser.getId()));
    }

    @Test
    public void getTop10PopularFilms() {
        Film film = Film.builder()
                .id(null)
                .name("Titanic")
                .description("descr")
                .releaseDate(LocalDate.of(2001, 01, 01))
                .duration(200)
                .userIdsLiked(new HashSet<>())
                .genres(new HashSet<>())
                .build();

        Film film2 = film.toBuilder()
                .name("The Expendables")
                .build();

        Film filmCreated = filmService.createFilm(film);
        Film repo = filmStorage.getFilmById(filmCreated.getId()).get();
        assertEquals(filmCreated.getId(), repo.getId());

        Film film2Created = filmService.createFilm(film2);
        Film repo2 = filmStorage.getFilmById(film2Created.getId()).get();
        assertEquals(film2Created.getId(), repo2.getId());

        User user1 = User.builder()
                .name("user")
                .birthday(LocalDate.now())
                .email("user@yandex.ru")
                .login("loginUser")
                .friendsIds(new HashSet<>())
                .build();

        User user2 = user1.toBuilder()
                .name("user 2")
                .build();

        User user3 = user1.toBuilder()
                .name("user 3")
                .build();

        User createdUser = userService.createUser(user1);
        User createdUser2 = userService.createUser(user2);
        User createdUser3 = userService.createUser(user3);

        //1 фильму ставят три лайка
        filmService.addLikeToFilm(filmCreated.getId(), createdUser.getId());
        filmService.addLikeToFilm(filmCreated.getId(), createdUser2.getId());
        filmService.addLikeToFilm(filmCreated.getId(), createdUser3.getId());

        //2 фильму 1 лайк
        filmService.addLikeToFilm(film2Created.getId(), createdUser3.getId());

        List<Film> topFilms = filmStorage.getTopPopularFilms(10).stream().toList();
        assertEquals(filmCreated.getId(), topFilms.get(0).getId());
        assertEquals(film2Created.getId(), topFilms.get(1).getId());
    }
}
