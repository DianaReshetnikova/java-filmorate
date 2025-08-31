package ru.yandex.practicum.filmorate.storage.dao;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.storage.interfaces.MpaStorage;
import ru.yandex.practicum.filmorate.storage.mappers.MpaRowMapper;

import java.util.Collection;
import java.util.Optional;

@Repository
public class MpaDbStorage implements MpaStorage {
    private JdbcTemplate jdbcTemplate;
    private MpaRowMapper mpaRowMapper;

    @Override
    public Collection<MPA> getMpa() {
        String FIND_ALL_QUERY = "SELECT * FROM mpa ORDER BY id";
        return jdbcTemplate.query(FIND_ALL_QUERY, mpaRowMapper);
    }

    @Override
    public Optional<MPA> getMpaById(Integer id) {
        String FIND_BY_ID = "SELECT * FROM mpa WHERE id = ?";
        try {
            MPA mpa = jdbcTemplate.queryForObject(FIND_BY_ID, mpaRowMapper, id);
            return Optional.ofNullable(mpa);
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }
}
