package com.example.myauth.controller.admin;

import com.example.myauth.dto.ApiResponse;
import com.example.myauth.dto.admin.dashboard.AdminDashboardSummaryResponse;
import com.example.myauth.service.admin.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

  private final AdminDashboardService adminDashboardService;

  @GetMapping("/summary")
  public ResponseEntity<ApiResponse<AdminDashboardSummaryResponse>> getDashboardSummary() {
    AdminDashboardSummaryResponse response = adminDashboardService.getSummary();
    return ResponseEntity.ok(ApiResponse.success("관리자 대시보드 요약 조회 성공", response));
  }
}
