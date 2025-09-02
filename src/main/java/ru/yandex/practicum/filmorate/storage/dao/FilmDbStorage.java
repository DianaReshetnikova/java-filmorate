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

        Integer mpaId = rs.getInt("mpa_id");
        if (mpaId != null) {
            var result = getMpaById(mpaId);
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
        String findAllQuery = "SELECT * FROM films";
        return jdbcTemplate.query(findAllQuery, filmRowMapper);
    }

    @Override
    public Optional<Film> getFilmById(Long id) {
        String findByIdQuery = "SELECT * FROM films WHERE id = ?";
        try {
            Film film = jdbcTemplate.queryForObject(findByIdQuery, filmRowMapper, id);
            return Optional.ofNullable(film);
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    @Override
    public Film createFilm(Film newFilm) {
        String insertQuery = "INSERT INTO films (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection
                    .prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
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
        String updateQuery = "UPDATE films SET name = ?," +
                " description = ?," +
                " release_date = ?," +
                " duration = ?," +
                " mpa_id = ?" +
                " WHERE id = ?";

        jdbcTemplate.update(updateQuery,
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
        String deleteByIdQuery = "DELETE FROM films WHERE id = ?";
        jdbcTemplate.update(deleteByIdQuery, id);
    }


    @Override
    public void addLikeToFilm(Long filmId, Long userId) {
        String insertNewFriendQuery = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";

        if (!isLikeAlreadyExist(filmId, userId)) {
            GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection
                        .prepareStatement(insertNewFriendQuery, Statement.RETURN_GENERATED_KEYS);
                ps.setLong(1, filmId);
                ps.setLong(2, userId);
                return ps;
            }, keyHolder);
        }
    }

    @Override
    public void removeLikeFromFilm(Long filmId, Long userId) {
        String deleteLikeQuery = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";

        if (isLikeAlreadyExist(filmId, userId)) {
            jdbcTemplate.update(deleteLikeQuery, filmId, userId);
        }
    }

    @Override
    public Collection<Film> getTopPopularFilms(Integer count) {
        String selectMostPopularFilmsQuery = """
                SELECT f.*
                FROM films f
                LEFT JOIN film_likes l ON f.id = l.film_id
                GROUP BY f.id
                ORDER BY COUNT(l.user_id) DESC
                LIMIT ?
                """;

        return jdbcTemplate.query(selectMostPopularFilmsQuery, filmRowMapper, count);
    }

    public Optional<MPA> getMpaById(Integer id) {
        String selectByIdQuery = "SELECT * FROM mpa WHERE id = ?";
        try {
            MPA mpa = jdbcTemplate.queryForObject(selectByIdQuery, mpaRowMapper, id);
            return Optional.ofNullable(mpa);
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    public List<Long> getUserIdsLikesOfFilm(Long id) {
        String selectUserLikesByFilmIdQuery = "SELECT user_id FROM film_likes WHERE film_id = ?";
        return jdbcTemplate.queryForList(selectUserLikesByFilmIdQuery, Long.class, id);
    }

    public List<Genre> getGenresOfFilmById(Long id) {
        String selectGenresOfFilmByIdQuery = "SELECT g.id, g.name " +
                " FROM genres AS g" +
                " JOIN film_genres AS fg" +
                " ON g.id = fg.genre_id" +
                " WHERE fg.film_id = ?" +
                " ORDER BY g.id";
        return jdbcTemplate.query(selectGenresOfFilmByIdQuery, genreRowMapper, id);
    }

    private void saveGenresOfFilm(Set<Genre> genres, Long filmId) {
        String findAllGenresIdQuery = "SELECT id FROM genres";
        String insertFilmGenreQuery = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";

        List<Integer> genresId = jdbcTemplate.queryForList(findAllGenresIdQuery, Integer.class);

        for (var genre : genres) {
            if (genresId.contains(genre.getId())) {
                if (!isFilmGenreAlreadyExist(filmId, genre.getId())) {
                    jdbcTemplate.update(insertFilmGenreQuery, filmId, genre.getId());
                }
            }
        }
    }

    private void saveLikesOfFilm(Set<Long> userIdsLikes, Long filmId) {
        String deleteByIdQuery = "DELETE FROM film_likes WHERE film_id = ?";
        jdbcTemplate.update(deleteByIdQuery, filmId);

        String INSERT_BY_ID_QUERY = "INSERT INTO film_likes WHERE film_id = ? AND user_id = ?";
        for (var userId : userIdsLikes) {
            jdbcTemplate.update(INSERT_BY_ID_QUERY, filmId, userId);
        }
    }

    private boolean isFilmGenreAlreadyExist(Long filmId, Integer genreId) {
        String isFriendAlreadyExistQuery = "SELECT COUNT(*) FROM film_genres" +
                " WHERE film_id = ?" +
                " AND genre_id = ?";
        try {
            int result = jdbcTemplate.queryForObject(isFriendAlreadyExistQuery, Integer.class, filmId, genreId);
            return result > 0;
        } catch (EmptyResultDataAccessException ignored) {
            return false;
        }
    }

    private boolean isLikeAlreadyExist(Long filmId, Long userId) {
        String isLikeAlreadyExistQuery = "SELECT COUNT(*) FROM film_likes" +
                " WHERE film_id = ?" +
                " AND user_id = ?";
        try {
            int result = jdbcTemplate.queryForObject(isLikeAlreadyExistQuery, Integer.class, filmId, userId);
            return result > 0;
        } catch (EmptyResultDataAccessException ignored) {
            return false;
        }
    }
}
