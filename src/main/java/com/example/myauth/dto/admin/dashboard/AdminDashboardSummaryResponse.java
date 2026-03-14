package com.example.myauth.dto.admin.dashboard;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminDashboardSummaryResponse {
  private Long totalUsers;
  private Long activeUsers;
  private Long suspendedUsers;
  private Long inactiveUsers;
  private Long totalPosts;
  private Long activePosts;
  private Long deletedPosts;
  private Long totalComments;
}
