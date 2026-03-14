package com.example.myauth.repository.specification;

import com.example.myauth.entity.Post;
import com.example.myauth.entity.User;
import com.example.myauth.entity.Visibility;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public final class PostSpecifications {

  private PostSpecifications() {
  }

  public static Specification<Post> keywordContains(String keyword) {
    return (root, query, cb) -> {
      if (keyword == null || keyword.isBlank()) {
        return cb.conjunction();
      }
      Join<Post, User> userJoin = root.join("user", JoinType.LEFT);
      String pattern = "%" + keyword.trim().toLowerCase() + "%";
      return cb.or(
          cb.like(cb.lower(root.get("content")), pattern),
          cb.like(cb.lower(userJoin.get("email")), pattern),
          cb.like(cb.lower(userJoin.get("name")), pattern)
      );
    };
  }

  public static Specification<Post> hasAuthorId(Long authorId) {
    return (root, query, cb) -> authorId == null
        ? cb.conjunction()
        : cb.equal(root.get("user").get("id"), authorId);
  }

  public static Specification<Post> authorEmailContains(String authorEmail) {
    return (root, query, cb) -> {
      if (authorEmail == null || authorEmail.isBlank()) {
        return cb.conjunction();
      }
      Join<Post, User> userJoin = root.join("user", JoinType.LEFT);
      return cb.like(cb.lower(userJoin.get("email")), "%" + authorEmail.trim().toLowerCase() + "%");
    };
  }

  public static Specification<Post> hasVisibility(Visibility visibility) {
    return (root, query, cb) -> visibility == null ? cb.conjunction() : cb.equal(root.get("visibility"), visibility);
  }

  public static Specification<Post> hasDeleted(Boolean isDeleted) {
    return (root, query, cb) -> isDeleted == null ? cb.conjunction() : cb.equal(root.get("isDeleted"), isDeleted);
  }

  public static Specification<Post> createdAtFrom(LocalDateTime createdFrom) {
    return (root, query, cb) -> createdFrom == null
        ? cb.conjunction()
        : cb.greaterThanOrEqualTo(root.get("createdAt"), createdFrom);
  }

  public static Specification<Post> createdAtTo(LocalDateTime createdTo) {
    return (root, query, cb) -> createdTo == null
        ? cb.conjunction()
        : cb.lessThanOrEqualTo(root.get("createdAt"), createdTo);
  }
}
