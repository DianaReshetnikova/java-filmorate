package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder(toBuilder = true)
public class Film {
    private Long id;
    @NotBlank(message = "Название фильма не может быть пустым")
    private String name;
    @Size(min = 0, max = 200, message = "Максимальная длина описания фильма — 200 символов")
    private String description;
    private LocalDate releaseDate;
    @PositiveOrZero(message = "Продолжительность фильма должна быть положительным числом")
    private int duration;
    //список Id пользователей которые поставили лайк фильму
    private final Set<Long> userIdsLiked = new HashSet<>();
}
