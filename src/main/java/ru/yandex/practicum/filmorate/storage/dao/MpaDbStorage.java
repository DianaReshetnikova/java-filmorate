package ru.yandex.practicum.filmorate.storage.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.storage.interfaces.MpaStorage;
import ru.yandex.practicum.filmorate.storage.mappers.MpaRowMapper;

import java.util.Collection;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MpaDbStorage implements MpaStorage {
    private final JdbcTemplate jdbcTemplate;
    private final MpaRowMapper mpaRowMapper;

    @Override
    public Collection<MPA> getMpa() {
        String findAllQuery = "SELECT * FROM mpa ORDER BY id";
        return jdbcTemplate.query(findAllQuery, mpaRowMapper);
    }

    @Override
    public Optional<MPA> getMpaById(Integer id) {
        String findById = "SELECT * FROM mpa WHERE id = ?";
        try {
            MPA mpa = jdbcTemplate.queryForObject(findById, mpaRowMapper, id);
            return Optional.ofNullable(mpa);
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }
}
