package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
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
    private MPA mpa;
    //список Id пользователей которые поставили лайк фильму
    private Set<Long> userIdsLiked = new HashSet<>();
    //список жанров фильма
    private Set<Genre> genres = new HashSet<>();
}
