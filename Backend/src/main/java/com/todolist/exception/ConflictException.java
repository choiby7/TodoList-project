package com.todolist.exception;

/**
 * 409 Conflict 예외
 * 리소스의 현재 상태와 충돌이 발생한 경우 (예: 중복된 이메일)
 */
public class ConflictException extends BaseException {

    public ConflictException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ConflictException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
