package com.example.myauth.dto.admin.user;

import com.example.myauth.entity.User;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AdminUserStatusUpdateRequest {

  @NotNull(message = "status는 필수입니다.")
  private User.Status status;

  private String reason;

  private LocalDateTime lockedUntil;
}
