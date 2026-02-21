package com.todolist.service;

import com.todolist.domain.Category;
import com.todolist.domain.Todo;
import com.todolist.domain.TodoPriority;
import com.todolist.domain.TodoStatus;
import com.todolist.dto.request.TodoCreateRequest;
import com.todolist.dto.request.TodoUpdateRequest;
import com.todolist.dto.response.PageResponse;
import com.todolist.dto.response.TodoResponse;
import com.todolist.exception.BadRequestException;
import com.todolist.exception.ErrorCode;
import com.todolist.exception.ForbiddenException;
import com.todolist.exception.ResourceNotFoundException;
import com.todolist.repository.CategoryRepository;
import com.todolist.repository.TodoRepository;
import com.todolist.specification.TodoSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Todo 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TodoService {

    private final TodoRepository todoRepository;
    private final CategoryRepository categoryRepository;

    /**
     * Todo 생성
     */
    @Transactional
    public TodoResponse createTodo(TodoCreateRequest request) {
        Long userId = getCurrentUserId();

        // 카테고리 검증 (선택 사항)
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findByCategoryIdAndUserId(request.getCategoryId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CATEGORY_NOT_FOUND));
        }

        Todo todo = Todo.builder()
                .userId(userId)
                .categoryId(request.getCategoryId())
                .title(request.getTitle())
                .description(request.getDescription())
                .priority(request.getPriority())
                .status(request.getStatus())
                .dueDate(request.getDueDate())
                .isImportant(request.getIsImportant())
                .build();

        Todo savedTodo = todoRepository.save(todo);
        log.info("Todo 생성 완료: todoId={}, userId={}, title={}", savedTodo.getTodoId(), userId, savedTodo.getTitle());

        return TodoResponse.from(savedTodo);
    }

    /**
     * Todo 목록 조회 (동적 필터링)
     */
    @Transactional(readOnly = true)
    public PageResponse<TodoResponse> getTodos(
            TodoStatus status, TodoPriority priority, Long categoryId,
            String keyword, LocalDateTime dueFrom, LocalDateTime dueTo,
            Pageable pageable) {

        Long userId = getCurrentUserId();

        // Specification을 사용한 동적 쿼리 생성
        Specification<Todo> spec = TodoSpecification.withFilters(
                userId, status, priority, categoryId, keyword, dueFrom, dueTo
        );

        Page<Todo> todoPage = todoRepository.findAll(spec, pageable);

        Page<TodoResponse> responsePage = todoPage.map(TodoResponse::from);
        log.info("Todo 목록 조회 완료: userId={}, totalElements={}", userId, todoPage.getTotalElements());
        return PageResponse.of(responsePage);
    }

    /**
     * Todo 상세 조회
     */
    @Transactional(readOnly = true)
    public TodoResponse getTodoById(Long todoId) {
        Long userId = getCurrentUserId();

        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.TODO_NOT_FOUND));

        // 소유권 검증
        if (!todo.getUserId().equals(userId)) {
            throw new ForbiddenException(ErrorCode.TODO_FORBIDDEN);
        }

        return TodoResponse.from(todo);
    }

    /**
     * Todo 수정
     */
    @Transactional
    public TodoResponse updateTodo(Long todoId, TodoUpdateRequest request) {
        Long userId = getCurrentUserId();

        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.TODO_NOT_FOUND));

        // 소유권 검증
        if (!todo.getUserId().equals(userId)) {
            throw new ForbiddenException(ErrorCode.TODO_FORBIDDEN);
        }

        // 카테고리 검증 (변경하는 경우)
        if (request.getCategoryId() != null) {
            categoryRepository.findByCategoryIdAndUserId(request.getCategoryId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CATEGORY_NOT_FOUND));
        }

        // 업데이트 (null이 아닌 필드만)
        todo.update(
                request.getTitle(),
                request.getDescription(),
                request.getPriority(),
                request.getStatus(),
                request.getDueDate(),
                request.getCategoryId(),
                request.getIsImportant()
        );

        Todo updatedTodo = todoRepository.save(todo);
        log.info("Todo 수정 완료: todoId={}, userId={}", todoId, userId);

        return TodoResponse.from(updatedTodo);
    }

    /**
     * Todo 소프트 삭제
     */
    @Transactional
    public void softDeleteTodo(Long todoId) {
        Long userId = getCurrentUserId();

        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.TODO_NOT_FOUND));

        // 소유권 검증
        if (!todo.getUserId().equals(userId)) {
            throw new ForbiddenException(ErrorCode.TODO_FORBIDDEN);
        }

        // 이미 삭제된 경우
        if (todo.getIsDeleted()) {
            throw new BadRequestException(ErrorCode.TODO_ALREADY_DELETED);
        }

        todo.softDelete();
        todoRepository.save(todo);
        log.info("Todo 소프트 삭제 완료: todoId={}, userId={}", todoId, userId);
    }

    /**
     * Todo 완료 상태 토글
     */
    @Transactional
    public TodoResponse toggleComplete(Long todoId) {
        Long userId = getCurrentUserId();

        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.TODO_NOT_FOUND));

        // 소유권 검증
        if (!todo.getUserId().equals(userId)) {
            throw new ForbiddenException(ErrorCode.TODO_FORBIDDEN);
        }

        todo.toggleComplete();
        Todo updatedTodo = todoRepository.save(todo);
        log.info("Todo 완료 토글: todoId={}, userId={}, status={}", todoId, userId, updatedTodo.getStatus());

        return TodoResponse.from(updatedTodo);
    }

    /**
     * 휴지통 목록 조회
     */
    @Transactional(readOnly = true)
    public PageResponse<TodoResponse> getTrashTodos(Pageable pageable) {
        Long userId = getCurrentUserId();

        Page<Todo> todoPage = todoRepository.findByUserIdAndIsDeletedTrue(userId, pageable);
        Page<TodoResponse> responsePage = todoPage.map(TodoResponse::from);
        return PageResponse.of(responsePage);
    }

    /**
     * 휴지통에서 복구
     */
    @Transactional
    public TodoResponse restoreTodo(Long todoId) {
        Long userId = getCurrentUserId();

        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.TODO_NOT_FOUND));

        // 소유권 검증
        if (!todo.getUserId().equals(userId)) {
            throw new ForbiddenException(ErrorCode.TODO_FORBIDDEN);
        }

        // 삭제되지 않은 경우
        if (!todo.getIsDeleted()) {
            throw new BadRequestException(ErrorCode.TODO_NOT_DELETED);
        }

        todo.restore();
        Todo restoredTodo = todoRepository.save(todo);
        log.info("Todo 복구 완료: todoId={}, userId={}", todoId, userId);

        return TodoResponse.from(restoredTodo);
    }

    /**
     * Todo 영구 삭제
     */
    @Transactional
    public void permanentDeleteTodo(Long todoId) {
        Long userId = getCurrentUserId();

        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.TODO_NOT_FOUND));

        // 소유권 검증
        if (!todo.getUserId().equals(userId)) {
            throw new ForbiddenException(ErrorCode.TODO_FORBIDDEN);
        }

        todoRepository.delete(todo);
        log.info("Todo 영구 삭제 완료: todoId={}, userId={}", todoId, userId);
    }

    /**
     * 휴지통 비우기
     */
    @Transactional
    public void emptyTrash() {
        Long userId = getCurrentUserId();

        List<Todo> trashTodos = todoRepository.findByUserIdAndIsDeletedTrue(userId, Pageable.unpaged())
                .getContent();

        todoRepository.deleteAll(trashTodos);
        log.info("휴지통 비우기 완료: userId={}, 삭제된 개수={}", userId, trashTodos.size());
    }

    /**
     * SecurityContext에서 현재 사용자 ID 추출
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return Long.parseLong(authentication.getName());
    }
}
