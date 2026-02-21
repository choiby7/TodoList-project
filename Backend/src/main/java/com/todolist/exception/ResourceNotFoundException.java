package com.todolist.exception;

/**
 * 404 Not Found 예외
 * 요청한 리소스를 찾을 수 없는 경우 발생
 */
public class ResourceNotFoundException extends BaseException {

    public ResourceNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ResourceNotFoundException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
