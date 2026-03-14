package com.example.myauth.service.admin;

import com.example.myauth.dto.admin.dashboard.AdminDashboardSummaryResponse;
import com.example.myauth.entity.User;
import com.example.myauth.repository.CommentRepository;
import com.example.myauth.repository.PostRepository;
import com.example.myauth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

  private final UserRepository userRepository;
  private final PostRepository postRepository;
  private final CommentRepository commentRepository;

  @Transactional(readOnly = true)
  public AdminDashboardSummaryResponse getSummary() {
    long totalUsers = userRepository.count();
    long activeUsers = userRepository.countByStatus(User.Status.ACTIVE);
    long suspendedUsers = userRepository.countByStatus(User.Status.SUSPENDED);
    long inactiveUsers = userRepository.countByStatus(User.Status.INACTIVE);

    long totalPosts = postRepository.count();
    long activePosts = postRepository.countByIsDeletedFalse();
    long deletedPosts = totalPosts - activePosts;

    long totalComments = commentRepository.count();

    return AdminDashboardSummaryResponse.builder()
        .totalUsers(totalUsers)
        .activeUsers(activeUsers)
        .suspendedUsers(suspendedUsers)
        .inactiveUsers(inactiveUsers)
        .totalPosts(totalPosts)
        .activePosts(activePosts)
        .deletedPosts(deletedPosts)
        .totalComments(totalComments)
        .build();
  }
}
