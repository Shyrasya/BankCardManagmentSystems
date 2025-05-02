package com.bank.cardmanagement.web.exception;

import com.bank.cardmanagement.exception.CardNotFoundException;
import com.bank.cardmanagement.exception.EmailAlreadyExistsException;
import com.bank.cardmanagement.exception.ResourceNotFoundException;
import com.bank.cardmanagement.exception.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Глобальный обработчик исключений для REST-контроллеров.
 * Перехватывает и обрабатывает типовые ошибки валидации, аутентификации и прочие исключения.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Обработка ошибок валидации аргументов запроса.
     *
     * @param e исключение, содержащее ошибки полей
     * @return карта с названиями полей и сообщениями об ошибках
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(errors);
    }

    /**
     * Обработка случаев, когда тело запроса отсутствует или JSON некорректен.
     *
     * @param e исключение сериализации JSON
     * @return сообщение об ошибке
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleMissingBody(HttpMessageNotReadableException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Отсутствует тело запроса или JSON некорректный!"));
    }

    /**
     * Обработка исключений с заданным HTTP-статусом.
     *
     * @param e исключение ResponseStatusException
     * @return сообщение об ошибке с указанным статусом
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<?> handleResponseStatusException(ResponseStatusException e) {
        return ResponseEntity
                .status(e.getStatusCode())
                .body(Map.of("error", Optional.ofNullable(e.getReason()).orElse("Неизвестная ошибка!")));
    }

    /**
     * Обработка ошибок аутентификации: неверные имя пользователя или пароль.
     *
     * @param e исключение, связанное с аутентификацией
     * @return сообщение об ошибке авторизации
     */
    @ExceptionHandler({UsernameNotFoundException.class, BadCredentialsException.class})
    public ResponseEntity<?> handleAuthExceptions(RuntimeException e) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Неверные учетные данные! " + e.getMessage()));
    }

    /**
     * Обработка случая, когда пользователь не найден.
     *
     * @param e исключение UserNotFoundException
     * @return сообщение об ошибке с 404 статусом
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<?> handleUserNotFound(UserNotFoundException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
    }

    /**
     * Обработка случая, когда карта не найдена.
     *
     * @param e исключение CardNotFoundException
     * @return сообщение об ошибке с 404 статусом
     */
    @ExceptionHandler(CardNotFoundException.class)
    public ResponseEntity<?> handleCardNotFound(CardNotFoundException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
    }

    /**
     * Обработка ошибок доступа (недостаточно прав).
     *
     * @param e исключение AccessDeniedException
     * @return сообщение об ошибке с 403 статусом
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDenied(AccessDeniedException e) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "Доступ запрещён: недостаточно прав! " + e.getMessage()));
    }

    /**
     * Обработка ошибок преобразования типов в аргументах запроса.
     *
     * @param e исключение при несовпадении типа аргумента
     * @return сообщение о неверном формате данных
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<?> handleTypeMismatchException(MethodArgumentTypeMismatchException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Неверный формат данных: " + e.getValue()));
    }

    /**
     * Обработка ошибок с некорректными аргументами.
     *
     * @param e исключение IllegalArgumentException
     * @return сообщение об ошибке с 400 статусом
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
    }

    /**
     * Обработка ошибок при отсутствии переменной пути.
     *
     * @param e исключение MissingPathVariableException
     * @return сообщение о недостающем параметре в пути
     */
    @ExceptionHandler(MissingPathVariableException.class)
    public ResponseEntity<Map<String, String>> handleMissingPathVariable(MissingPathVariableException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Обязательный параметр в пути не найден: " + e.getVariableName()));
    }

    /**
     * Обработка запросов к несуществующим маршрутам.
     *
     * @param e исключение NoHandlerFoundException
     * @return сообщение о некорректном пути запроса
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Map<String, String>> handleNoHandlerFoundException(NoHandlerFoundException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Некорректный запрос: отсутствует обязательный параметр в пути! " + e.getMessage()));
    }

    /**
     * Обработка случая, когда пользователь пытается зарегистрироваться с уже существующим email.
     *
     * @param e исключение EmailAlreadyExistsException
     * @return сообщение об ошибке с 409 статусом (CONFLICT)
     */
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<String> handleEmailAlreadyExistsException(EmailAlreadyExistsException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(e.getMessage());
    }

    /**
     * Обработка случая, когда запрашиваемый ресурс не найден.
     *
     * @param e исключение ResourceNotFoundException
     * @return сообщение об ошибке с 404 статусом
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> handleResourceNotFoundException(ResourceNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(e.getMessage());
    }

    /**
     * Обработка некорректного состояния приложения.
     *
     * @param e исключение IllegalStateException
     * @return сообщение об ошибке с 409 статусом (CONFLICT)
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleIllegalStateException(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(e.getMessage());
    }

    /**
     * Обработка всех неожиданных исключений.
     *
     * @param e любое необработанное исключение
     * @return сообщение о внутренней ошибке сервера с 500 статусом
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleUnexpectedException(Exception e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Внутренняя ошибка сервера! " + e.getMessage()));
    }
}
