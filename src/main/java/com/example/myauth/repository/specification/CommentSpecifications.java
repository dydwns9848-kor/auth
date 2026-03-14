package com.example.myauth.repository.specification;

import com.example.myauth.entity.Comment;
import com.example.myauth.entity.User;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public final class CommentSpecifications {

  private CommentSpecifications() {
  }

  public static Specification<Comment> keywordContains(String keyword) {
    return (root, query, cb) -> {
      if (keyword == null || keyword.isBlank()) {
        return cb.conjunction();
      }
      Join<Comment, User> userJoin = root.join("user", JoinType.LEFT);
      String pattern = "%" + keyword.trim().toLowerCase() + "%";
      return cb.or(
          cb.like(cb.lower(root.get("content")), pattern),
          cb.like(cb.lower(userJoin.get("email")), pattern),
          cb.like(cb.lower(userJoin.get("name")), pattern)
      );
    };
  }

  public static Specification<Comment> hasPostId(Long postId) {
    return (root, query, cb) -> postId == null
        ? cb.conjunction()
        : cb.equal(root.get("post").get("id"), postId);
  }

  public static Specification<Comment> hasAuthorId(Long authorId) {
    return (root, query, cb) -> authorId == null
        ? cb.conjunction()
        : cb.equal(root.get("user").get("id"), authorId);
  }

  public static Specification<Comment> hasAuthorEmail(String authorEmail) {
    return (root, query, cb) -> {
      if (authorEmail == null || authorEmail.isBlank()) {
        return cb.conjunction();
      }
      Join<Comment, User> userJoin = root.join("user", JoinType.LEFT);
      return cb.like(cb.lower(userJoin.get("email")), "%" + authorEmail.trim().toLowerCase() + "%");
    };
  }

  public static Specification<Comment> hasDeleted(Boolean isDeleted) {
    return (root, query, cb) -> isDeleted == null ? cb.conjunction() : cb.equal(root.get("isDeleted"), isDeleted);
  }

  public static Specification<Comment> createdAtFrom(LocalDateTime createdFrom) {
    return (root, query, cb) -> createdFrom == null
        ? cb.conjunction()
        : cb.greaterThanOrEqualTo(root.get("createdAt"), createdFrom);
  }

  public static Specification<Comment> createdAtTo(LocalDateTime createdTo) {
    return (root, query, cb) -> createdTo == null
        ? cb.conjunction()
        : cb.lessThanOrEqualTo(root.get("createdAt"), createdTo);
  }
}
