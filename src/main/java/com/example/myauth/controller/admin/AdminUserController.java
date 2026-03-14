package com.example.myauth.controller.admin;

import com.example.myauth.dto.ApiResponse;
import com.example.myauth.dto.admin.user.AdminUserDetailResponse;
import com.example.myauth.dto.admin.user.AdminUserListResponse;
import com.example.myauth.dto.admin.user.AdminUserRoleUpdateRequest;
import com.example.myauth.dto.admin.user.AdminUserStatusUpdateRequest;
import com.example.myauth.entity.User;
import com.example.myauth.service.admin.AdminUserService;
import jakarta.validation.Valid;
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
@RequestMapping("/api/admin/users")
public class AdminUserController {

  private final AdminUserService adminUserService;

  @GetMapping
  public ResponseEntity<ApiResponse<Page<AdminUserListResponse>>> getUsers(
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) User.Status status,
      @RequestParam(required = false) User.Role role,
      @RequestParam(required = false) String provider,
      @RequestParam(required = false) Boolean isActive,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdFrom,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdTo,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size
  ) {
    if (size > 100) {
      size = 100;
    }

    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
    Page<AdminUserListResponse> users = adminUserService.getUsers(
        keyword,
        status,
        role,
        provider,
        isActive,
        toStartOfDay(createdFrom),
        toEndOfDay(createdTo),
        pageable
    );

    return ResponseEntity.ok(ApiResponse.success("관리자 사용자 목록 조회 성공", users));
  }

  @GetMapping("/{userId}")
  public ResponseEntity<ApiResponse<AdminUserDetailResponse>> getUserDetail(
      @PathVariable Long userId
  ) {
    AdminUserDetailResponse response = adminUserService.getUserDetail(userId);
    return ResponseEntity.ok(ApiResponse.success("관리자 사용자 상세 조회 성공", response));
  }

  @PatchMapping("/{userId}/status")
  public ResponseEntity<ApiResponse<AdminUserDetailResponse>> updateUserStatus(
      @AuthenticationPrincipal User adminUser,
      @PathVariable Long userId,
      @Valid @RequestBody AdminUserStatusUpdateRequest request
  ) {
    AdminUserDetailResponse response = adminUserService.updateUserStatus(adminUser, userId, request);
    return ResponseEntity.ok(ApiResponse.success("사용자 상태 변경 성공", response));
  }

  @PatchMapping("/{userId}/role")
  public ResponseEntity<ApiResponse<AdminUserDetailResponse>> updateUserRole(
      @AuthenticationPrincipal User adminUser,
      @PathVariable Long userId,
      @Valid @RequestBody AdminUserRoleUpdateRequest request
  ) {
    AdminUserDetailResponse response = adminUserService.updateUserRole(adminUser, userId, request);
    return ResponseEntity.ok(ApiResponse.success("사용자 권한 변경 성공", response));
  }

  private LocalDateTime toStartOfDay(LocalDate date) {
    return date == null ? null : date.atStartOfDay();
  }

  private LocalDateTime toEndOfDay(LocalDate date) {
    return date == null ? null : date.atTime(23, 59, 59);
  }
}
