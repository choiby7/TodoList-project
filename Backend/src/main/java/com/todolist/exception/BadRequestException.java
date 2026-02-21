package com.todolist.exception;

/**
 * 400 Bad Request 예외
 * 잘못된 요청 파라미터나 데이터인 경우 발생
 */
public class BadRequestException extends BaseException {

    public BadRequestException(ErrorCode errorCode) {
        super(errorCode);
    }

    public BadRequestException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
