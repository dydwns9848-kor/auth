package com.example.myauth.repository;

import com.example.myauth.entity.DmRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DmRoomRepository extends JpaRepository<DmRoom, Long> {

  Optional<DmRoom> findByUser1IdAndUser2Id(Long user1Id, Long user2Id);

  @Query("SELECT r FROM DmRoom r " +
      "JOIN FETCH r.user1 " +
      "JOIN FETCH r.user2 " +
      "WHERE r.id = :roomId")
  Optional<DmRoom> findByIdWithUsers(@Param("roomId") Long roomId);

  @Query(
      value = "SELECT r FROM DmRoom r " +
          "JOIN FETCH r.user1 " +
          "JOIN FETCH r.user2 " +
          "WHERE r.user1.id = :meId OR r.user2.id = :meId " +
          "ORDER BY CASE WHEN r.lastMessageAt IS NULL THEN 1 ELSE 0 END ASC, r.lastMessageAt DESC",
      countQuery = "SELECT COUNT(r) FROM DmRoom r WHERE r.user1.id = :meId OR r.user2.id = :meId"
  )
  Page<DmRoom> findMyRooms(@Param("meId") Long meId, Pageable pageable);
}
