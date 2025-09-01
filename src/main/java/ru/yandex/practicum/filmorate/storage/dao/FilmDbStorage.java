package ru.yandex.practicum.filmorate.storage.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.storage.interfaces.FilmStorage;
import ru.yandex.practicum.filmorate.storage.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.MpaRowMapper;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Types;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    private final MpaRowMapper mpaRowMapper;
    private final GenreRowMapper genreRowMapper;
    private final RowMapper<Film> filmRowMapper = (rs, rowNum) -> {
        Film film = new Film();

        film.setId(rs.getLong("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        Date releaseDate = rs.getDate("release_date");
        film.setReleaseDate(releaseDate.toLocalDate());
        film.setDuration(rs.getInt("duration"));

        Integer mpa_id = rs.getInt("mpa_id");
        if (mpa_id != null) {
            var result = getMpaById(mpa_id);
            if (result.isPresent())
                film.setMpa(result.get());
            else
                film.setMpa(null);
        } else {
            film.setMpa(null);
        }

        Set<Long> userLikes = new HashSet<>(getUserIdsLikesOfFilm(film.getId()));
        film.setUserIdsLiked(userLikes);

        Set<Genre> filmGenres = new HashSet<>(getGenresOfFilmById(film.getId()));
        film.setGenres(filmGenres);

        return film;
    };


    @Override
    public Collection<Film> getFilms() {
        String FIND_ALL_QUERY = "SELECT * FROM films";
        return jdbcTemplate.query(FIND_ALL_QUERY, filmRowMapper);
    }

    @Override
    public Optional<Film> getFilmById(Long id) {
        String FIND_BY_ID = "SELECT * FROM films WHERE id = ?";
        try {
            Film film = jdbcTemplate.queryForObject(FIND_BY_ID, filmRowMapper, id);
            return Optional.ofNullable(film);
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    @Override
    public Film createFilm(Film newFilm) {
        String INSERT_QUERY = "INSERT INTO films (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection
                    .prepareStatement(INSERT_QUERY, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, newFilm.getName());
            ps.setString(2, newFilm.getDescription());
            ps.setDate(3, Date.valueOf(newFilm.getReleaseDate()));
            ps.setInt(4, newFilm.getDuration());
            if (newFilm.getMpa() != null) {
                ps.setInt(5, newFilm.getMpa().getId());
            } else
                ps.setNull(5, Types.INTEGER);

            return ps;
        }, keyHolder);

        Long id = keyHolder.getKeyAs(Long.class);
        //сгенерированный id нового фильма
        newFilm.setId(id);

        saveLikesOfFilm(newFilm.getUserIdsLiked(), newFilm.getId());
        saveGenresOfFilm(newFilm.getGenres(), newFilm.getId());
        return newFilm;
    }

    @Override
    public Film updateFilm(Film newFilm) {
        String UPDATE_QUERY = "UPDATE films SET name = ?," +
                " description = ?," +
                " release_date = ?," +
                " duration = ?," +
                " mpa_id = ?" +
                " WHERE id = ?";

        jdbcTemplate.update(UPDATE_QUERY,
                newFilm.getName(),
                newFilm.getDescription(),
                newFilm.getReleaseDate(),
                newFilm.getDuration(),
                newFilm.getMpa() != null ? newFilm.getMpa().getId() : null,
                newFilm.getId());

        saveLikesOfFilm(newFilm.getUserIdsLiked(), newFilm.getId());
        saveGenresOfFilm(newFilm.getGenres(), newFilm.getId());

        return newFilm;
    }

    @Override
    public void deleteFilm(Long id) {
        String DELETE_BY_ID_QUERY = "DELETE FROM films WHERE id = ?";
        jdbcTemplate.update(DELETE_BY_ID_QUERY, id);
    }


    @Override
    public void addLikeToFilm(Long filmId, Long userId) {
        String INSERT_NEW_FRIEND_QUERY = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";

        if (!isLikeAlreadyExist(filmId, userId)) {
            GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection
                        .prepareStatement(INSERT_NEW_FRIEND_QUERY, Statement.RETURN_GENERATED_KEYS);
                ps.setLong(1, filmId);
                ps.setLong(2, userId);
                return ps;
            }, keyHolder);
        }
    }

    @Override
    public void removeLikeFromFilm(Long filmId, Long userId) {
        String DELETE_LIKE_QUERY = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";

        if (isLikeAlreadyExist(filmId, userId)) {
            jdbcTemplate.update(DELETE_LIKE_QUERY, filmId, userId);
        }
    }

    @Override
    public Collection<Film> getTopPopularFilms(Integer count) {
        String SELECT_MOST_POPULAR_FILMS_QUERY = """
                SELECT f.*
                FROM films f
                LEFT JOIN film_likes l ON f.id = l.film_id
                GROUP BY f.id
                ORDER BY COUNT(l.user_id) DESC
                LIMIT ?
                """;

        return jdbcTemplate.query(SELECT_MOST_POPULAR_FILMS_QUERY, filmRowMapper, count);
    }

    public Optional<MPA> getMpaById(Integer id) {
        String SELECT_BY_ID_QUERY = "SELECT * FROM mpa WHERE id = ?";
        try {
            MPA mpa = jdbcTemplate.queryForObject(SELECT_BY_ID_QUERY, mpaRowMapper, id);
            return Optional.ofNullable(mpa);
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    public List<Long> getUserIdsLikesOfFilm(Long id) {
        String SELECT_USER_LIKES_BY_FILM_ID_QUERY = "SELECT user_id FROM film_likes WHERE film_id = ?";
        return jdbcTemplate.queryForList(SELECT_USER_LIKES_BY_FILM_ID_QUERY, Long.class, id);
    }

    public List<Genre> getGenresOfFilmById(Long id) {
        String SELECT_GENRES_OF_FILM_BY_ID_QUERY = "SELECT g.id, g.name " +
                " FROM genres AS g" +
                " JOIN film_genres AS fg" +
                " ON g.id = fg.genre_id" +
                " WHERE fg.film_id = ?" +
                " ORDER BY g.id";
        return jdbcTemplate.query(SELECT_GENRES_OF_FILM_BY_ID_QUERY, genreRowMapper, id);
    }

    private void saveGenresOfFilm(Set<Genre> genres, Long filmId) {
        String FIND_ALL_GENRES_ID_QUERY = "SELECT id FROM genres";
        String INSERT_FILM_GENRE_QUERY = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";

        List<Integer> genresId = jdbcTemplate.queryForList(FIND_ALL_GENRES_ID_QUERY, Integer.class);

        for (var genre : genres) {
            if (genresId.contains(genre.getId())) {
                if (!isFilmGenreAlreadyExist(filmId, genre.getId())) {
                    jdbcTemplate.update(INSERT_FILM_GENRE_QUERY, filmId, genre.getId());
                }
            }
        }
    }

    private void saveLikesOfFilm(Set<Long> userIdsLikes, Long filmId) {
        String DELETE_BY_ID_QUERY = "DELETE FROM film_likes WHERE film_id = ?";
        jdbcTemplate.update(DELETE_BY_ID_QUERY, filmId);

        String INSERT_BY_ID_QUERY = "INSERT INTO film_likes WHERE film_id = ? AND user_id = ?";
        for (var userId : userIdsLikes) {
            jdbcTemplate.update(INSERT_BY_ID_QUERY, filmId, userId);
        }
    }

    private boolean isFilmGenreAlreadyExist(Long filmId, Integer genreId) {
        String IS_FRIEND_ALREADY_EXIST_QUERY = "SELECT COUNT(*) FROM film_genres" +
                " WHERE film_id = ?" +
                " AND genre_id = ?";
        try {
            int result = jdbcTemplate.queryForObject(IS_FRIEND_ALREADY_EXIST_QUERY, Integer.class, filmId, genreId);
            return result > 0;
        } catch (EmptyResultDataAccessException ignored) {
            return false;
        }
    }

    private boolean isLikeAlreadyExist(Long filmId, Long userId) {
        String IS_LIKE_ALREADY_EXIST_QUERY = "SELECT COUNT(*) FROM film_likes" +
                " WHERE film_id = ?" +
                " AND user_id = ?";
        try {
            int result = jdbcTemplate.queryForObject(IS_LIKE_ALREADY_EXIST_QUERY, Integer.class, filmId, userId);
            return result > 0;
        } catch (EmptyResultDataAccessException ignored) {
            return false;
        }
    }
}
