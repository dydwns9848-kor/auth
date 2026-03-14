package com.example.myauth.service.admin;

import com.example.myauth.dto.admin.user.AdminUserDetailResponse;
import com.example.myauth.dto.admin.user.AdminUserListResponse;
import com.example.myauth.dto.admin.user.AdminUserRoleUpdateRequest;
import com.example.myauth.dto.admin.user.AdminUserStatusUpdateRequest;
import com.example.myauth.entity.AdminAuditLog;
import com.example.myauth.entity.User;
import com.example.myauth.exception.AdminAccessDeniedException;
import com.example.myauth.exception.ModerationPolicyException;
import com.example.myauth.exception.UserNotFoundException;
import com.example.myauth.repository.UserRepository;
import com.example.myauth.repository.specification.UserSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminUserService {

  private final UserRepository userRepository;
  private final AdminAuditLogService adminAuditLogService;

  @Transactional(readOnly = true)
  public Page<AdminUserListResponse> getUsers(
      String keyword,
      User.Status status,
      User.Role role,
      String provider,
      Boolean isActive,
      LocalDateTime createdFrom,
      LocalDateTime createdTo,
      Pageable pageable
  ) {
    Specification<User> spec = UserSpecifications.keywordContains(keyword)
        .and(UserSpecifications.hasStatus(status))
        .and(UserSpecifications.hasRole(role))
        .and(UserSpecifications.hasProvider(provider))
        .and(UserSpecifications.isActive(isActive))
        .and(UserSpecifications.createdAtFrom(createdFrom))
        .and(UserSpecifications.createdAtTo(createdTo));

    return userRepository.findAll(spec, pageable).map(AdminUserListResponse::from);
  }

  @Transactional(readOnly = true)
  public AdminUserDetailResponse getUserDetail(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException(userId));
    return AdminUserDetailResponse.from(user);
  }

  @Transactional
  public AdminUserDetailResponse updateUserStatus(
      User adminUser,
      Long targetUserId,
      AdminUserStatusUpdateRequest request
  ) {
    validateAdmin(adminUser);
    User target = userRepository.findById(targetUserId)
        .orElseThrow(() -> new UserNotFoundException(targetUserId));

    validateNotSelfTarget(adminUser, target);
    validateAdminTargetAccess(adminUser, target);

    if (request.getStatus() == User.Status.SUSPENDED
        && request.getLockedUntil() != null
        && request.getLockedUntil().isBefore(LocalDateTime.now())) {
      throw new ModerationPolicyException("정지 종료 시각은 현재 시각 이후여야 합니다.");
    }

    String beforeData = snapshot(target);
    applyStatus(target, request.getStatus(), request.getLockedUntil());
    userRepository.save(target);

    adminAuditLogService.record(
        adminUser.getId(),
        toActionType(request.getStatus()),
        AdminAuditLog.TargetType.USER,
        target.getId(),
        request.getReason(),
        beforeData,
        snapshot(target)
    );

    return AdminUserDetailResponse.from(target);
  }

  @Transactional
  public AdminUserDetailResponse updateUserRole(
      User adminUser,
      Long targetUserId,
      AdminUserRoleUpdateRequest request
  ) {
    validateAdmin(adminUser);
    validateSuperAdmin(adminUser);

    User target = userRepository.findById(targetUserId)
        .orElseThrow(() -> new UserNotFoundException(targetUserId));

    validateNotSelfTarget(adminUser, target);

    if (request.getRole() == User.Role.ROLE_USER && Boolean.TRUE.equals(request.getIsSuperUser())) {
      throw new ModerationPolicyException("ROLE_USER 계정은 슈퍼관리자가 될 수 없습니다.");
    }

    boolean willRemoveSuperUser = target.getIsSuperUser()
        && (request.getRole() == User.Role.ROLE_USER || Boolean.FALSE.equals(request.getIsSuperUser()));
    if (willRemoveSuperUser) {
      long superAdminCount = userRepository.countByRoleAndIsSuperUserTrue(User.Role.ROLE_ADMIN);
      if (superAdminCount <= 1) {
        throw new ModerationPolicyException("마지막 슈퍼관리자는 강등할 수 없습니다.");
      }
    }

    String beforeData = snapshot(target);
    target.setRole(request.getRole());
    if (request.getRole() == User.Role.ROLE_USER) {
      target.setIsSuperUser(false);
    } else if (request.getIsSuperUser() != null) {
      target.setIsSuperUser(request.getIsSuperUser());
    }

    userRepository.save(target);

    adminAuditLogService.record(
        adminUser.getId(),
        AdminAuditLog.ActionType.USER_ROLE_CHANGED,
        AdminAuditLog.TargetType.USER,
        target.getId(),
        request.getReason(),
        beforeData,
        snapshot(target)
    );

    return AdminUserDetailResponse.from(target);
  }

  private void validateAdmin(User adminUser) {
    if (adminUser.getRole() != User.Role.ROLE_ADMIN) {
      throw new AdminAccessDeniedException("관리자 권한이 필요합니다.");
    }
  }

  private void validateSuperAdmin(User adminUser) {
    if (!Boolean.TRUE.equals(adminUser.getIsSuperUser())) {
      throw new AdminAccessDeniedException("해당 작업은 슈퍼관리자만 수행할 수 있습니다.");
    }
  }

  private void validateNotSelfTarget(User adminUser, User target) {
    if (adminUser.getId().equals(target.getId())) {
      throw new ModerationPolicyException("본인 계정은 변경할 수 없습니다.");
    }
  }

  private void validateAdminTargetAccess(User adminUser, User target) {
    boolean targetIsAdmin = target.getRole() == User.Role.ROLE_ADMIN;
    boolean targetIsSuperAdmin = Boolean.TRUE.equals(target.getIsSuperUser());
    boolean actorIsSuperAdmin = Boolean.TRUE.equals(adminUser.getIsSuperUser());

    if ((targetIsAdmin || targetIsSuperAdmin) && !actorIsSuperAdmin) {
      throw new AdminAccessDeniedException("일반 관리자는 관리자 계정을 변경할 수 없습니다.");
    }
  }

  private void applyStatus(User target, User.Status status, LocalDateTime lockedUntil) {
    target.setStatus(status);
    switch (status) {
      case ACTIVE -> {
        target.setIsActive(true);
        target.setAccountLockedUntil(null);
      }
      case SUSPENDED -> {
        target.setIsActive(false);
        target.setAccountLockedUntil(lockedUntil);
      }
      case INACTIVE, DELETED, PENDING_VERIFICATION -> {
        target.setIsActive(false);
        target.setAccountLockedUntil(null);
      }
    }
  }

  private AdminAuditLog.ActionType toActionType(User.Status status) {
    return switch (status) {
      case SUSPENDED -> AdminAuditLog.ActionType.USER_SUSPENDED;
      case ACTIVE -> AdminAuditLog.ActionType.USER_ACTIVATED;
      case INACTIVE -> AdminAuditLog.ActionType.USER_DEACTIVATED;
      case DELETED -> AdminAuditLog.ActionType.USER_DELETED;
      case PENDING_VERIFICATION -> AdminAuditLog.ActionType.USER_STATUS_CHANGED;
    };
  }

  private String snapshot(User user) {
    return "role=" + user.getRole()
        + ", status=" + user.getStatus()
        + ", isActive=" + user.getIsActive()
        + ", isSuperUser=" + user.getIsSuperUser()
        + ", accountLockedUntil=" + user.getAccountLockedUntil();
  }
}
