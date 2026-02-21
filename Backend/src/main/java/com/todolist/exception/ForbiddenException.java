package com.todolist.exception;

/**
 * 403 Forbidden 예외
 * 인증은 되었으나 권한이 없는 리소스에 접근한 경우 발생
 */
public class ForbiddenException extends BaseException {

    public ForbiddenException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ForbiddenException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
