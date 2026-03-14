package com.example.myauth.dto.admin.user;

import com.example.myauth.entity.User;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminUserRoleUpdateRequest {

  @NotNull(message = "role은 필수입니다.")
  private User.Role role;

  private Boolean isSuperUser;

  private String reason;
}
