package com.example.myauth.service.admin;

import com.example.myauth.dto.admin.post.AdminPostDetailResponse;
import com.example.myauth.dto.admin.post.AdminPostListResponse;
import com.example.myauth.entity.AdminAuditLog;
import com.example.myauth.entity.Post;
import com.example.myauth.entity.User;
import com.example.myauth.entity.Visibility;
import com.example.myauth.exception.AdminAccessDeniedException;
import com.example.myauth.exception.ModerationPolicyException;
import com.example.myauth.exception.PostNotFoundException;
import com.example.myauth.repository.PostRepository;
import com.example.myauth.repository.specification.PostSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminPostService {

  private final PostRepository postRepository;
  private final AdminAuditLogService adminAuditLogService;

  @Transactional(readOnly = true)
  public Page<AdminPostListResponse> getPosts(
      String keyword,
      Long authorId,
      String authorEmail,
      Visibility visibility,
      Boolean isDeleted,
      LocalDateTime createdFrom,
      LocalDateTime createdTo,
      Pageable pageable
  ) {
    Specification<Post> spec = PostSpecifications.keywordContains(keyword)
        .and(PostSpecifications.hasAuthorId(authorId))
        .and(PostSpecifications.authorEmailContains(authorEmail))
        .and(PostSpecifications.hasVisibility(visibility))
        .and(PostSpecifications.hasDeleted(isDeleted))
        .and(PostSpecifications.createdAtFrom(createdFrom))
        .and(PostSpecifications.createdAtTo(createdTo));

    return postRepository.findAll(spec, pageable).map(AdminPostListResponse::from);
  }

  @Transactional(readOnly = true)
  public AdminPostDetailResponse getPost(Long postId) {
    Post post = postRepository.findByIdWithUserAndImagesForAdmin(postId)
        .orElseThrow(() -> new PostNotFoundException(postId));
    return AdminPostDetailResponse.from(post);
  }

  @Transactional
  public void deletePost(User adminUser, Long postId, String reason) {
    validateAdmin(adminUser);
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new PostNotFoundException(postId));

    if (Boolean.TRUE.equals(post.getIsDeleted())) {
      throw new ModerationPolicyException("이미 삭제된 게시물입니다.");
    }

    String beforeData = snapshot(post);
    post.softDelete();
    postRepository.save(post);

    adminAuditLogService.record(
        adminUser.getId(),
        AdminAuditLog.ActionType.POST_DELETED,
        AdminAuditLog.TargetType.POST,
        post.getId(),
        reason,
        beforeData,
        snapshot(post)
    );
  }

  private void validateAdmin(User adminUser) {
    if (adminUser.getRole() != User.Role.ROLE_ADMIN) {
      throw new AdminAccessDeniedException("관리자 권한이 필요합니다.");
    }
  }

  private String snapshot(Post post) {
    return "isDeleted=" + post.getIsDeleted()
        + ", visibility=" + post.getVisibility()
        + ", contentLength=" + (post.getContent() == null ? 0 : post.getContent().length());
  }
}
