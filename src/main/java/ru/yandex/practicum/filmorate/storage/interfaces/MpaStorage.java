package ru.yandex.practicum.filmorate.storage.interfaces;

import ru.yandex.practicum.filmorate.model.MPA;

import java.util.Collection;
import java.util.Optional;

public interface MpaStorage {
    public Collection<MPA> getMpa();

    public Optional<MPA> getMpaById(Integer id);
}
