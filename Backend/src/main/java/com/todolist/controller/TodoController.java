package com.todolist.controller;

import com.todolist.domain.TodoPriority;
import com.todolist.domain.TodoStatus;
import com.todolist.dto.request.TodoCreateRequest;
import com.todolist.dto.request.TodoUpdateRequest;
import com.todolist.dto.response.ApiResponse;
import com.todolist.dto.response.PageResponse;
import com.todolist.dto.response.TodoResponse;
import com.todolist.service.TodoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * Todo 컨트롤러
 */
@Tag(name = "Todo", description = "할 일 관리 API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/todos")
@RequiredArgsConstructor
public class TodoController {

    private final TodoService todoService;

    @Operation(summary = "Todo 목록 조회", description = "사용자의 Todo 목록을 조회합니다. 다양한 필터링 옵션을 지원합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<TodoResponse>>> getTodos(
            @Parameter(description = "상태 필터 (TODO, IN_PROGRESS, COMPLETED)")
            @RequestParam(required = false) TodoStatus status,

            @Parameter(description = "우선순위 필터 (LOW, MEDIUM, HIGH)")
            @RequestParam(required = false) TodoPriority priority,

            @Parameter(description = "카테고리 ID 필터")
            @RequestParam(required = false) Long categoryId,

            @Parameter(description = "검색 키워드 (제목, 설명)")
            @RequestParam(required = false) String keyword,

            @Parameter(description = "마감일 시작 범위")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dueFrom,

            @Parameter(description = "마감일 종료 범위")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dueTo,

            @Parameter(description = "페이지 번호 (0부터 시작)")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "페이지 크기")
            @RequestParam(defaultValue = "20") int size,

            @Parameter(description = "정렬 기준 (createdAt, dueDate, priority, title)")
            @RequestParam(defaultValue = "createdAt") String sortBy,

            @Parameter(description = "정렬 방향 (asc, desc)")
            @RequestParam(defaultValue = "desc") String sortOrder) {

        Sort.Direction direction = "asc".equalsIgnoreCase(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        PageResponse<TodoResponse> response = todoService.getTodos(
                status, priority, categoryId, keyword, dueFrom, dueTo, pageable
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Todo 생성", description = "새로운 할 일을 생성합니다")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "생성 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효성 검증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<TodoResponse>> createTodo(@Valid @RequestBody TodoCreateRequest request) {
        TodoResponse response = todoService.createTodo(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @Operation(summary = "Todo 상세 조회", description = "특정 할 일의 상세 정보를 조회합니다")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "리소스 없음")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TodoResponse>> getTodoById(@PathVariable Long id) {
        TodoResponse response = todoService.getTodoById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Todo 수정", description = "할 일 정보를 수정합니다")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효성 검증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "리소스 없음")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TodoResponse>> updateTodo(
            @PathVariable Long id,
            @Valid @RequestBody TodoUpdateRequest request) {
        TodoResponse response = todoService.updateTodo(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Todo 삭제 (소프트 삭제)", description = "할 일을 휴지통으로 이동합니다")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "리소스 없음")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDeleteTodo(@PathVariable Long id) {
        todoService.softDeleteTodo(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Todo 완료 상태 토글", description = "할 일의 완료 상태를 토글합니다")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "토글 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "리소스 없음")
    })
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<ApiResponse<TodoResponse>> toggleComplete(@PathVariable Long id) {
        TodoResponse response = todoService.toggleComplete(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "휴지통 목록 조회", description = "삭제된 할 일 목록을 조회합니다")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @GetMapping("/trash")
    public ResponseEntity<ApiResponse<PageResponse<TodoResponse>>> getTrashTodos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "deletedAt"));
        PageResponse<TodoResponse> response = todoService.getTrashTodos(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "휴지통에서 복구", description = "삭제된 할 일을 복구합니다")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "복구 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "리소스 없음")
    })
    @PatchMapping("/{id}/restore")
    public ResponseEntity<ApiResponse<TodoResponse>> restoreTodo(@PathVariable Long id) {
        TodoResponse response = todoService.restoreTodo(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Todo 영구 삭제", description = "할 일을 완전히 삭제합니다")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "리소스 없음")
    })
    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<Void> permanentDeleteTodo(@PathVariable Long id) {
        todoService.permanentDeleteTodo(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "휴지통 비우기", description = "모든 삭제된 할 일을 영구 삭제합니다")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "비우기 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @DeleteMapping("/trash/empty")
    public ResponseEntity<Void> emptyTrash() {
        todoService.emptyTrash();
        return ResponseEntity.noContent().build();
    }
}
