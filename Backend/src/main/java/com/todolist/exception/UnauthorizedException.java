package com.todolist.exception;

/**
 * 401 Unauthorized 예외
 * 인증이 필요하거나 인증에 실패한 경우 발생
 */
public class UnauthorizedException extends BaseException {

    public UnauthorizedException(ErrorCode errorCode) {
        super(errorCode);
    }

    public UnauthorizedException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
