package com.example.myauth.dto.admin.comment;

import com.example.myauth.entity.Comment;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminCommentDetailResponse {
  private Long id;
  private Long postId;
  private Long authorId;
  private String authorEmail;
  private String authorName;
  private Long parentId;
  private String content;
  private Integer likeCount;
  private Boolean isDeleted;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public static AdminCommentDetailResponse from(Comment comment) {
    return AdminCommentDetailResponse.builder()
        .id(comment.getId())
        .postId(comment.getPost().getId())
        .authorId(comment.getUser().getId())
        .authorEmail(comment.getUser().getEmail())
        .authorName(comment.getUser().getName())
        .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
        .content(comment.getContent())
        .likeCount(comment.getLikeCount())
        .isDeleted(comment.getIsDeleted())
        .createdAt(comment.getCreatedAt())
        .updatedAt(comment.getUpdatedAt())
        .build();
  }
}
