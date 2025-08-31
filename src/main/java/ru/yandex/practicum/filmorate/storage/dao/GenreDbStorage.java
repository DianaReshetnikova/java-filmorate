package ru.yandex.practicum.filmorate.storage.dao;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.interfaces.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mappers.GenreRowMapper;

import java.util.Collection;
import java.util.Optional;

@Repository
public class GenreDbStorage implements GenreStorage {
    private JdbcTemplate jdbcTemplate;
    private GenreRowMapper genreRowMapper;

    @Override
    public Collection<Genre> getGenres() {
        String FIND_ALL_QUERY = "SELECT * FROM genres ORDER BY id";
        return jdbcTemplate.query(FIND_ALL_QUERY, genreRowMapper);
    }

    @Override
    public Optional<Genre> getGenreById(Integer id) {
        String FIND_BY_ID = "SELECT * FROM genres WHERE id = ?";
        try {
            Genre mpa = jdbcTemplate.queryForObject(FIND_BY_ID, genreRowMapper, id);
            return Optional.ofNullable(mpa);
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }
}
