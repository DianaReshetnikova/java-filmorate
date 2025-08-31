package ru.yandex.practicum.filmorate.exception;

public class InvalidJsonFieldException extends RuntimeException {
    public InvalidJsonFieldException(String message) {
        super(message);
    }
}
