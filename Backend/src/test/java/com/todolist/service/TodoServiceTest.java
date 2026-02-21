package com.todolist.service;

import com.todolist.domain.Todo;
import com.todolist.domain.TodoPriority;
import com.todolist.domain.TodoStatus;
import com.todolist.dto.request.TodoCreateRequest;
import com.todolist.dto.response.PageResponse;
import com.todolist.dto.response.TodoResponse;
import com.todolist.exception.ErrorCode;
import com.todolist.exception.ForbiddenException;
import com.todolist.exception.ResourceNotFoundException;
import com.todolist.repository.CategoryRepository;
import com.todolist.repository.TodoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * TodoService 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TodoService 단위 테스트")
class TodoServiceTest {

    @Mock
    private TodoRepository todoRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private TodoService todoService;

    private static final Long TEST_USER_ID = 1L;
    private static final Long OTHER_USER_ID = 2L;

    @BeforeEach
    void setUp() {
        // SecurityContext 설정
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(TEST_USER_ID.toString());
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("Todo 생성 - 성공")
    void createTodo_WhenValidRequest_ShouldReturnTodoResponse() {
        // Given
        TodoCreateRequest request = TodoCreateRequest.builder()
                .title("테스트 할일")
                .description("테스트 설명")
                .priority(TodoPriority.HIGH)
                .status(TodoStatus.TODO)
                .isImportant(true)
                .build();

        Todo savedTodo = Todo.builder()
                .userId(TEST_USER_ID)
                .title(request.getTitle())
                .description(request.getDescription())
                .priority(request.getPriority())
                .status(request.getStatus())
                .isImportant(request.getIsImportant())
                .build();

        when(todoRepository.save(any(Todo.class))).thenReturn(savedTodo);

        // When
        TodoResponse response = todoService.createTodo(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo(request.getTitle());
        assertThat(response.getDescription()).isEqualTo(request.getDescription());
        assertThat(response.getPriority()).isEqualTo(request.getPriority());
        assertThat(response.getStatus()).isEqualTo(request.getStatus());
        assertThat(response.getIsImportant()).isEqualTo(request.getIsImportant());

        verify(todoRepository, times(1)).save(any(Todo.class));
    }

    @Test
    @DisplayName("Todo 목록 조회 - 필터 없이 전체 조회 성공")
    void getTodos_WhenNoFilters_ShouldReturnAllTodos() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);

        List<Todo> todos = Arrays.asList(
                createMockTodo(1L, "할일 1", TodoStatus.TODO, TodoPriority.HIGH),
                createMockTodo(2L, "할일 2", TodoStatus.IN_PROGRESS, TodoPriority.MEDIUM),
                createMockTodo(3L, "할일 3", TodoStatus.COMPLETED, TodoPriority.LOW)
        );
        Page<Todo> todoPage = new PageImpl<>(todos, pageable, todos.size());

        when(todoRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(todoPage);

        // When
        PageResponse<TodoResponse> response = todoService.getTodos(
                null, null, null, null, null, null, pageable
        );

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(3);
        assertThat(response.getTotalElements()).isEqualTo(3);
        assertThat(response.getTotalPages()).isEqualTo(1);

        verify(todoRepository, times(1)).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    @DisplayName("Todo 목록 조회 - status 필터링 성공")
    void getTodos_WhenStatusFilter_ShouldReturnFilteredTodos() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        TodoStatus filterStatus = TodoStatus.COMPLETED;

        List<Todo> todos = Arrays.asList(
                createMockTodo(1L, "완료된 할일 1", TodoStatus.COMPLETED, TodoPriority.HIGH),
                createMockTodo(2L, "완료된 할일 2", TodoStatus.COMPLETED, TodoPriority.MEDIUM)
        );
        Page<Todo> todoPage = new PageImpl<>(todos, pageable, todos.size());

        when(todoRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(todoPage);

        // When
        PageResponse<TodoResponse> response = todoService.getTodos(
                filterStatus, null, null, null, null, null, pageable
        );

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(2);
        assertThat(response.getContent())
                .allMatch(todo -> todo.getStatus() == TodoStatus.COMPLETED);

        verify(todoRepository, times(1)).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    @DisplayName("Todo 목록 조회 - priority 필터링 성공")
    void getTodos_WhenPriorityFilter_ShouldReturnFilteredTodos() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        TodoPriority filterPriority = TodoPriority.HIGH;

        List<Todo> todos = Arrays.asList(
                createMockTodo(1L, "높은 우선순위 1", TodoStatus.TODO, TodoPriority.HIGH),
                createMockTodo(2L, "높은 우선순위 2", TodoStatus.IN_PROGRESS, TodoPriority.HIGH)
        );
        Page<Todo> todoPage = new PageImpl<>(todos, pageable, todos.size());

        when(todoRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(todoPage);

        // When
        PageResponse<TodoResponse> response = todoService.getTodos(
                null, filterPriority, null, null, null, null, pageable
        );

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(2);
        assertThat(response.getContent())
                .allMatch(todo -> todo.getPriority() == TodoPriority.HIGH);

        verify(todoRepository, times(1)).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    @DisplayName("Todo 목록 조회 - categoryId 필터링 성공")
    void getTodos_WhenCategoryFilter_ShouldReturnFilteredTodos() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        Long categoryId = 10L;

        Todo todo = createMockTodo(1L, "카테고리 할일", TodoStatus.TODO, TodoPriority.MEDIUM);
        List<Todo> todos = Arrays.asList(todo);
        Page<Todo> todoPage = new PageImpl<>(todos, pageable, todos.size());

        when(todoRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(todoPage);

        // When
        PageResponse<TodoResponse> response = todoService.getTodos(
                null, null, categoryId, null, null, null, pageable
        );

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);

        verify(todoRepository, times(1)).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    @DisplayName("Todo 상세 조회 - 성공")
    void getTodoById_WhenValidId_ShouldReturnTodo() {
        // Given
        Long todoId = 1L;
        Todo todo = createMockTodo(todoId, "테스트 할일", TodoStatus.TODO, TodoPriority.HIGH);

        when(todoRepository.findById(todoId)).thenReturn(Optional.of(todo));

        // When
        TodoResponse response = todoService.getTodoById(todoId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getTodoId()).isEqualTo(todoId);
        assertThat(response.getTitle()).isEqualTo("테스트 할일");

        verify(todoRepository, times(1)).findById(todoId);
    }

    @Test
    @DisplayName("Todo 상세 조회 - 존재하지 않는 ID로 조회 시 예외 발생")
    void getTodoById_WhenNotFound_ShouldThrowException() {
        // Given
        Long todoId = 999L;
        when(todoRepository.findById(todoId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> todoService.getTodoById(todoId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TODO_NOT_FOUND);

        verify(todoRepository, times(1)).findById(todoId);
    }

    @Test
    @DisplayName("Todo 상세 조회 - 다른 사용자의 Todo 접근 시 403 예외")
    void getTodoById_WhenAccessedByOtherUser_ShouldThrowForbiddenException() {
        // Given
        Long todoId = 1L;
        // 다른 사용자의 Todo 생성
        Todo todo = Todo.builder()
                .userId(OTHER_USER_ID)  // 다른 사용자 ID
                .title("다른 사용자 할일")
                .description("다른 사용자의 설명")
                .priority(TodoPriority.HIGH)
                .status(TodoStatus.TODO)
                .isImportant(false)
                .build();

        when(todoRepository.findById(todoId)).thenReturn(Optional.of(todo));

        // When & Then
        assertThatThrownBy(() -> todoService.getTodoById(todoId))
                .isInstanceOf(ForbiddenException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TODO_FORBIDDEN);

        verify(todoRepository, times(1)).findById(todoId);
    }

    @Test
    @DisplayName("Todo 완료 토글 - 성공")
    void toggleComplete_WhenValidTodo_ShouldToggleStatus() {
        // Given
        Long todoId = 1L;
        Todo todo = createMockTodo(todoId, "테스트 할일", TodoStatus.TODO, TodoPriority.HIGH);

        when(todoRepository.findById(todoId)).thenReturn(Optional.of(todo));
        when(todoRepository.save(any(Todo.class))).thenReturn(todo);

        // When
        TodoResponse response = todoService.toggleComplete(todoId);

        // Then
        assertThat(response).isNotNull();
        verify(todoRepository, times(1)).findById(todoId);
        verify(todoRepository, times(1)).save(any(Todo.class));
    }

    @Test
    @DisplayName("Todo 소프트 삭제 - 성공")
    void softDeleteTodo_WhenValidTodo_ShouldMarkAsDeleted() {
        // Given
        Long todoId = 1L;
        Todo todo = createMockTodo(todoId, "테스트 할일", TodoStatus.TODO, TodoPriority.HIGH);

        when(todoRepository.findById(todoId)).thenReturn(Optional.of(todo));
        when(todoRepository.save(any(Todo.class))).thenReturn(todo);

        // When
        todoService.softDeleteTodo(todoId);

        // Then
        verify(todoRepository, times(1)).findById(todoId);
        verify(todoRepository, times(1)).save(any(Todo.class));
    }

    /**
     * Mock Todo 객체 생성 헬퍼 메서드
     */
    private Todo createMockTodo(Long todoId, String title, TodoStatus status, TodoPriority priority) {
        Todo todo = Todo.builder()
                .userId(TEST_USER_ID)
                .title(title)
                .description("테스트 설명")
                .status(status)
                .priority(priority)
                .isImportant(false)
                .build();

        // Reflection을 사용해서 @GeneratedValue ID 설정
        ReflectionTestUtils.setField(todo, "todoId", todoId);

        return todo;
    }
}
