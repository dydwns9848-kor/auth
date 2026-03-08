package com.example.myauth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@DynamicInsert
@DynamicUpdate
@Table(
    name = "dm_rooms",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_dm_rooms_users", columnNames = {"user1_id", "user2_id"})
    },
    indexes = {
        @Index(name = "idx_dm_rooms_last_message_at", columnList = "last_message_at DESC"),
        @Index(name = "idx_dm_rooms_user1", columnList = "user1_id"),
        @Index(name = "idx_dm_rooms_user2", columnList = "user2_id")
    }
)
@Check(constraints = "user1_id < user2_id")
public class DmRoom {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user1_id", nullable = false)
  private User user1;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user2_id", nullable = false)
  private User user2;

  @Column(name = "last_message_id")
  private Long lastMessageId;

  @Column(name = "last_message_at")
  private LocalDateTime lastMessageAt;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @PrePersist
  @PreUpdate
  private void validateUserOrder() {
    if (user1 == null || user2 == null || user1.getId() == null || user2.getId() == null) {
      return;
    }
    if (user1.getId() >= user2.getId()) {
      throw new IllegalStateException("dm_rooms requires user1_id < user2_id");
    }
  }

  public static DmRoom of(User a, User b) {
    if (a == null || b == null || a.getId() == null || b.getId() == null) {
      throw new IllegalArgumentException("Both users and user ids are required");
    }
    if (a.getId().equals(b.getId())) {
      throw new IllegalArgumentException("Cannot create DM room with same user");
    }
    return a.getId() < b.getId()
        ? DmRoom.builder().user1(a).user2(b).build()
        : DmRoom.builder().user1(b).user2(a).build();
  }
}
