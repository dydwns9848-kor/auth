package com.example.myauth.dto.admin.user;

import com.example.myauth.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminUserListResponse {
  private Long id;
  private String email;
  private String name;
  private User.Role role;
  private User.Status status;
  private Boolean isActive;
  private Boolean isSuperUser;
  private String provider;
  private LocalDateTime createdAt;
  private LocalDateTime lastLoginAt;

  public static AdminUserListResponse from(User user) {
    return AdminUserListResponse.builder()
        .id(user.getId())
        .email(user.getEmail())
        .name(user.getName())
        .role(user.getRole())
        .status(user.getStatus())
        .isActive(user.getIsActive())
        .isSuperUser(user.getIsSuperUser())
        .provider(user.getProvider())
        .createdAt(user.getCreatedAt())
        .lastLoginAt(user.getLastLoginAt())
        .build();
  }
}
