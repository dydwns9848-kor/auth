package com.example.myauth.controller.admin;

import com.example.myauth.dto.ApiResponse;
import com.example.myauth.dto.admin.comment.AdminCommentDetailResponse;
import com.example.myauth.dto.admin.comment.AdminCommentListResponse;
import com.example.myauth.dto.admin.common.AdminDeleteRequest;
import com.example.myauth.entity.User;
import com.example.myauth.service.admin.AdminCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/comments")
public class AdminCommentController {

  private final AdminCommentService adminCommentService;

  @GetMapping
  public ResponseEntity<ApiResponse<Page<AdminCommentListResponse>>> getComments(
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) Long postId,
      @RequestParam(required = false) Long authorId,
      @RequestParam(required = false) String authorEmail,
      @RequestParam(required = false) Boolean isDeleted,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdFrom,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdTo,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size
  ) {
    if (size > 100) {
      size = 100;
    }

    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
    Page<AdminCommentListResponse> comments = adminCommentService.getComments(
        keyword,
        postId,
        authorId,
        authorEmail,
        isDeleted,
        toStartOfDay(createdFrom),
        toEndOfDay(createdTo),
        pageable
    );

    return ResponseEntity.ok(ApiResponse.success("관리자 댓글 목록 조회 성공", comments));
  }

  @GetMapping("/{commentId}")
  public ResponseEntity<ApiResponse<AdminCommentDetailResponse>> getComment(
      @PathVariable Long commentId
  ) {
    AdminCommentDetailResponse response = adminCommentService.getComment(commentId);
    return ResponseEntity.ok(ApiResponse.success("관리자 댓글 상세 조회 성공", response));
  }

  @DeleteMapping("/{commentId}")
  public ResponseEntity<ApiResponse<Void>> deleteComment(
      @AuthenticationPrincipal User adminUser,
      @PathVariable Long commentId,
      @RequestBody(required = false) AdminDeleteRequest request
  ) {
    String reason = request != null ? request.getReason() : null;
    adminCommentService.deleteComment(adminUser, commentId, reason);
    return ResponseEntity.ok(ApiResponse.success("관리자 댓글 삭제 성공"));
  }

  private LocalDateTime toStartOfDay(LocalDate date) {
    return date == null ? null : date.atStartOfDay();
  }

  private LocalDateTime toEndOfDay(LocalDate date) {
    return date == null ? null : date.atTime(23, 59, 59);
  }
}
