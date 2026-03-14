package com.example.myauth.dto.admin.post;

import com.example.myauth.entity.Post;
import com.example.myauth.entity.Visibility;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminPostListResponse {
  private Long id;
  private Long authorId;
  private String authorEmail;
  private String authorName;
  private String content;
  private Visibility visibility;
  private Integer likeCount;
  private Integer commentCount;
  private Integer viewCount;
  private Boolean isDeleted;
  private LocalDateTime createdAt;

  public static AdminPostListResponse from(Post post) {
    return AdminPostListResponse.builder()
        .id(post.getId())
        .authorId(post.getUser().getId())
        .authorEmail(post.getUser().getEmail())
        .authorName(post.getUser().getName())
        .content(post.getContent())
        .visibility(post.getVisibility())
        .likeCount(post.getLikeCount())
        .commentCount(post.getCommentCount())
        .viewCount(post.getViewCount())
        .isDeleted(post.getIsDeleted())
        .createdAt(post.getCreatedAt())
        .build();
  }
}
