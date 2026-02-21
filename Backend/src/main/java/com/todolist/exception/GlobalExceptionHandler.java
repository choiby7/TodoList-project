package com.todolist.exception;

import com.todolist.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 전역 예외 처리 핸들러
 * 애플리케이션에서 발생하는 모든 예외를 일관된 형식으로 처리합니다.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 401 Unauthorized 예외 처리
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedException(
            UnauthorizedException e,
            HttpServletRequest request) {
        log.warn("UnauthorizedException: {} at {}", e.getMessage(), request.getRequestURI());

        ErrorResponse errorResponse = ErrorResponse.of(
                e.getErrorCode().getCode(),
                e.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(errorResponse);
    }

    /**
     * 403 Forbidden 예외 처리
     */
    @ExceptionHandler({ForbiddenException.class, AccessDeniedException.class})
    public ResponseEntity<ErrorResponse> handleForbiddenException(
            Exception e,
            HttpServletRequest request) {
        log.warn("ForbiddenException: {} at {}", e.getMessage(), request.getRequestURI());

        String errorCode = ErrorCode.TODO_FORBIDDEN.getCode();
        String message = e.getMessage();

        if (e instanceof ForbiddenException) {
            ForbiddenException fe = (ForbiddenException) e;
            errorCode = fe.getErrorCode().getCode();
        }

        ErrorResponse errorResponse = ErrorResponse.of(
                errorCode,
                message,
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(errorResponse);
    }

    /**
     * 404 Not Found 예외 처리
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException e,
            HttpServletRequest request) {
        log.warn("ResourceNotFoundException: {} at {}", e.getMessage(), request.getRequestURI());

        ErrorResponse errorResponse = ErrorResponse.of(
                e.getErrorCode().getCode(),
                e.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorResponse);
    }

    /**
     * 409 Conflict 예외 처리
     */
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflictException(
            ConflictException e,
            HttpServletRequest request) {
        log.warn("ConflictException: {} at {}", e.getMessage(), request.getRequestURI());

        ErrorResponse errorResponse = ErrorResponse.of(
                e.getErrorCode().getCode(),
                e.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(errorResponse);
    }

    /**
     * 400 Bad Request 예외 처리
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequestException(
            BadRequestException e,
            HttpServletRequest request) {
        log.warn("BadRequestException: {} at {}", e.getMessage(), request.getRequestURI());

        ErrorResponse errorResponse = ErrorResponse.of(
                e.getErrorCode().getCode(),
                e.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    /**
     * Bean Validation 예외 처리 (@Valid 검증 실패)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e,
            HttpServletRequest request) {
        log.warn("MethodArgumentNotValidException at {}", request.getRequestURI());

        BindingResult bindingResult = e.getBindingResult();

        List<ErrorResponse.FieldError> fieldErrors = bindingResult.getFieldErrors().stream()
                .map(error -> ErrorResponse.FieldError.builder()
                        .field(error.getField())
                        .message(error.getDefaultMessage())
                        .rejectedValue(error.getRejectedValue())
                        .build())
                .collect(Collectors.toList());

        ErrorResponse errorResponse = ErrorResponse.of(
                ErrorCode.COMMON_INVALID_PARAMETER.getCode(),
                ErrorCode.COMMON_INVALID_PARAMETER.getMessage(),
                fieldErrors,
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    /**
     * 메서드 인자 타입 불일치 예외 처리
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException e,
            HttpServletRequest request) {
        log.warn("MethodArgumentTypeMismatchException: {} at {}", e.getMessage(), request.getRequestURI());

        String message = String.format("'%s' 파라미터의 값 '%s'은(는) 올바른 형식이 아닙니다",
                e.getName(), e.getValue());

        ErrorResponse errorResponse = ErrorResponse.of(
                ErrorCode.COMMON_INVALID_PARAMETER.getCode(),
                message,
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    /**
     * 예상치 못한 모든 예외 처리
     * 내부 상세 정보는 로그에만 기록하고, 사용자에게는 일반적인 메시지만 반환
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(
            Exception e,
            HttpServletRequest request) {
        log.error("Unexpected exception at {}: {}", request.getRequestURI(), e.getMessage(), e);

        ErrorResponse errorResponse = ErrorResponse.of(
                ErrorCode.COMMON_INTERNAL_ERROR.getCode(),
                ErrorCode.COMMON_INTERNAL_ERROR.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
    }
}
