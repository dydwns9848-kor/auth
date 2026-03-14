package com.example.myauth.service.admin;

import com.example.myauth.dto.admin.comment.AdminCommentDetailResponse;
import com.example.myauth.dto.admin.comment.AdminCommentListResponse;
import com.example.myauth.entity.AdminAuditLog;
import com.example.myauth.entity.Comment;
import com.example.myauth.entity.User;
import com.example.myauth.exception.AdminAccessDeniedException;
import com.example.myauth.exception.CommentNotFoundException;
import com.example.myauth.exception.ModerationPolicyException;
import com.example.myauth.repository.CommentRepository;
import com.example.myauth.repository.PostRepository;
import com.example.myauth.repository.specification.CommentSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminCommentService {

  private final CommentRepository commentRepository;
  private final PostRepository postRepository;
  private final AdminAuditLogService adminAuditLogService;

  @Transactional(readOnly = true)
  public Page<AdminCommentListResponse> getComments(
      String keyword,
      Long postId,
      Long authorId,
      String authorEmail,
      Boolean isDeleted,
      LocalDateTime createdFrom,
      LocalDateTime createdTo,
      Pageable pageable
  ) {
    Specification<Comment> spec = CommentSpecifications.keywordContains(keyword)
        .and(CommentSpecifications.hasPostId(postId))
        .and(CommentSpecifications.hasAuthorId(authorId))
        .and(CommentSpecifications.hasAuthorEmail(authorEmail))
        .and(CommentSpecifications.hasDeleted(isDeleted))
        .and(CommentSpecifications.createdAtFrom(createdFrom))
        .and(CommentSpecifications.createdAtTo(createdTo));

    return commentRepository.findAll(spec, pageable).map(AdminCommentListResponse::from);
  }

  @Transactional(readOnly = true)
  public AdminCommentDetailResponse getComment(Long commentId) {
    Comment comment = commentRepository.findByIdWithUserAndPostForAdmin(commentId)
        .orElseThrow(() -> new CommentNotFoundException(commentId));
    return AdminCommentDetailResponse.from(comment);
  }

  @Transactional
  public void deleteComment(User adminUser, Long commentId, String reason) {
    validateAdmin(adminUser);
    Comment comment = commentRepository.findByIdWithUserAndPostForAdmin(commentId)
        .orElseThrow(() -> new CommentNotFoundException(commentId));

    if (Boolean.TRUE.equals(comment.getIsDeleted())) {
      throw new ModerationPolicyException("이미 삭제된 댓글입니다.");
    }

    String beforeData = snapshot(comment);
    comment.softDelete();
    commentRepository.save(comment);
    postRepository.decrementCommentCount(comment.getPost().getId());

    adminAuditLogService.record(
        adminUser.getId(),
        AdminAuditLog.ActionType.COMMENT_DELETED,
        AdminAuditLog.TargetType.COMMENT,
        comment.getId(),
        reason,
        beforeData,
        snapshot(comment)
    );
  }

  private void validateAdmin(User adminUser) {
    if (adminUser.getRole() != User.Role.ROLE_ADMIN) {
      throw new AdminAccessDeniedException("관리자 권한이 필요합니다.");
    }
  }

  private String snapshot(Comment comment) {
    return "isDeleted=" + comment.getIsDeleted()
        + ", postId=" + comment.getPost().getId()
        + ", parentId=" + (comment.getParent() == null ? null : comment.getParent().getId())
        + ", contentLength=" + (comment.getContent() == null ? 0 : comment.getContent().length());
  }
}
