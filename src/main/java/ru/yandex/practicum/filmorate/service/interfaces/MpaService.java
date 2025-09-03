package ru.yandex.practicum.filmorate.service.interfaces;

import ru.yandex.practicum.filmorate.model.MPA;

import java.util.Collection;

public interface MpaService {
    Collection<MPA> getMpa();

    MPA getMpaById(Integer id);
}
