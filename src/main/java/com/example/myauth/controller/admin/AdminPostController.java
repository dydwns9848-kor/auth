package com.example.myauth.controller.admin;

import com.example.myauth.dto.ApiResponse;
import com.example.myauth.dto.admin.common.AdminDeleteRequest;
import com.example.myauth.dto.admin.post.AdminPostDetailResponse;
import com.example.myauth.dto.admin.post.AdminPostListResponse;
import com.example.myauth.entity.User;
import com.example.myauth.entity.Visibility;
import com.example.myauth.service.admin.AdminPostService;
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
@RequestMapping("/api/admin/posts")
public class AdminPostController {

  private final AdminPostService adminPostService;

  @GetMapping
  public ResponseEntity<ApiResponse<Page<AdminPostListResponse>>> getPosts(
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) Long authorId,
      @RequestParam(required = false) String authorEmail,
      @RequestParam(required = false) Visibility visibility,
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
    Page<AdminPostListResponse> posts = adminPostService.getPosts(
        keyword,
        authorId,
        authorEmail,
        visibility,
        isDeleted,
        toStartOfDay(createdFrom),
        toEndOfDay(createdTo),
        pageable
    );

    return ResponseEntity.ok(ApiResponse.success("관리자 게시물 목록 조회 성공", posts));
  }

  @GetMapping("/{postId}")
  public ResponseEntity<ApiResponse<AdminPostDetailResponse>> getPost(
      @PathVariable Long postId
  ) {
    AdminPostDetailResponse response = adminPostService.getPost(postId);
    return ResponseEntity.ok(ApiResponse.success("관리자 게시물 상세 조회 성공", response));
  }

  @DeleteMapping("/{postId}")
  public ResponseEntity<ApiResponse<Void>> deletePost(
      @AuthenticationPrincipal User adminUser,
      @PathVariable Long postId,
      @RequestBody(required = false) AdminDeleteRequest request
  ) {
    String reason = request != null ? request.getReason() : null;
    adminPostService.deletePost(adminUser, postId, reason);
    return ResponseEntity.ok(ApiResponse.success("관리자 게시물 삭제 성공"));
  }

  private LocalDateTime toStartOfDay(LocalDate date) {
    return date == null ? null : date.atStartOfDay();
  }

  private LocalDateTime toEndOfDay(LocalDate date) {
    return date == null ? null : date.atTime(23, 59, 59);
  }
}
