package com.example.myauth.repository.specification;

import com.example.myauth.entity.User;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public final class UserSpecifications {

  private UserSpecifications() {
  }

  public static Specification<User> keywordContains(String keyword) {
    return (root, query, cb) -> {
      if (keyword == null || keyword.isBlank()) {
        return cb.conjunction();
      }
      String pattern = "%" + keyword.trim().toLowerCase() + "%";
      return cb.or(
          cb.like(cb.lower(root.get("email")), pattern),
          cb.like(cb.lower(root.get("name")), pattern)
      );
    };
  }

  public static Specification<User> hasStatus(User.Status status) {
    return (root, query, cb) -> status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
  }

  public static Specification<User> hasRole(User.Role role) {
    return (root, query, cb) -> role == null ? cb.conjunction() : cb.equal(root.get("role"), role);
  }

  public static Specification<User> hasProvider(String provider) {
    return (root, query, cb) -> {
      if (provider == null || provider.isBlank()) {
        return cb.conjunction();
      }
      return cb.equal(cb.lower(root.get("provider")), provider.trim().toLowerCase());
    };
  }

  public static Specification<User> isActive(Boolean isActive) {
    return (root, query, cb) -> isActive == null ? cb.conjunction() : cb.equal(root.get("isActive"), isActive);
  }

  public static Specification<User> createdAtFrom(LocalDateTime createdFrom) {
    return (root, query, cb) -> createdFrom == null
        ? cb.conjunction()
        : cb.greaterThanOrEqualTo(root.get("createdAt"), createdFrom);
  }

  public static Specification<User> createdAtTo(LocalDateTime createdTo) {
    return (root, query, cb) -> createdTo == null
        ? cb.conjunction()
        : cb.lessThanOrEqualTo(root.get("createdAt"), createdTo);
  }
}
