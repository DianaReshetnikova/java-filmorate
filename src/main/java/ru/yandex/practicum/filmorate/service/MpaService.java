package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.storage.interfaces.MpaStorage;

import java.util.Collection;

// Указываем, что класс является бином и его
// нужно добавить в контекст приложения
@Service
@Slf4j
@RequiredArgsConstructor
public class MpaService {
    private final MpaStorage mpaStorage;

    public Collection<MPA> getMpa() {
        return mpaStorage.getMpa();
    }

    public MPA getMpaById(Integer id) {
        return mpaStorage.getMpaById(id).orElseThrow(() -> new NotFoundException("Возрастной рейтинг с id = " + id + " не найден"));
    }
}
