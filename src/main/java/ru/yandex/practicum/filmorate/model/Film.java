package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

/**
 * Film.
 */
@Data
@Builder
public class Film {
    Long id;
    @NotBlank(message = "Название фильма не может быть пустым")
    String name;
    @Size(min = 0, max = 200, message = "Максимальная длина описания фильма — 200 символов")
    String description;
    LocalDate releaseDate;
    @PositiveOrZero(message = "Продолжительность фильма должна быть положительным числом")
    int duration;
}
