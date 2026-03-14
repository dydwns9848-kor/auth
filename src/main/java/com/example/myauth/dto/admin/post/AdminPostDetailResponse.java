package com.example.myauth.dto.admin.post;

import com.example.myauth.entity.Post;
import com.example.myauth.entity.Visibility;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class AdminPostDetailResponse {
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
  private LocalDateTime updatedAt;
  private List<String> imageUrls;

  public static AdminPostDetailResponse from(Post post) {
    return AdminPostDetailResponse.builder()
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
        .updatedAt(post.getUpdatedAt())
        .imageUrls(post.getImages().stream().map(image -> image.getImageUrl()).toList())
        .build();
  }
}
