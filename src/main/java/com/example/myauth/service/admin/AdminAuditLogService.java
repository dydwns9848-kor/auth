package com.example.myauth.service.admin;

import com.example.myauth.entity.AdminAuditLog;
import com.example.myauth.entity.User;
import com.example.myauth.repository.AdminAuditLogRepository;
import com.example.myauth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminAuditLogService {

  private final AdminAuditLogRepository adminAuditLogRepository;
  private final UserRepository userRepository;

  @Transactional
  public void record(
      Long adminUserId,
      AdminAuditLog.ActionType actionType,
      AdminAuditLog.TargetType targetType,
      Long targetId,
      String reason,
      String beforeData,
      String afterData
  ) {
    User adminUser = userRepository.findById(adminUserId)
        .orElseThrow(() -> new IllegalArgumentException("관리자 사용자를 찾을 수 없습니다."));

    AdminAuditLog auditLog = AdminAuditLog.builder()
        .adminUser(adminUser)
        .actionType(actionType)
        .targetType(targetType)
        .targetId(targetId)
        .reason(reason)
        .beforeData(beforeData)
        .afterData(afterData)
        .build();

    adminAuditLogRepository.save(auditLog);
  }
}
