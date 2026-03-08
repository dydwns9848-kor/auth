package com.example.myauth.repository;

import com.example.myauth.entity.DmMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DmMessageRepository extends JpaRepository<DmMessage, Long> {

  Slice<DmMessage> findByRoomIdOrderByIdDesc(Long roomId, Pageable pageable);

  Slice<DmMessage> findByRoomIdAndIdLessThanOrderByIdDesc(Long roomId, Long beforeId, Pageable pageable);

  long countByRoomIdAndSenderIdNot(Long roomId, Long senderId);

  @Query("SELECT COUNT(m) FROM DmMessage m " +
      "WHERE m.room.id = :roomId " +
      "AND m.sender.id <> :senderId " +
      "AND m.id > :lastReadMessageId")
  long countUnreadAfter(
      @Param("roomId") Long roomId,
      @Param("senderId") Long senderId,
      @Param("lastReadMessageId") Long lastReadMessageId
  );
}
