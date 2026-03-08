package com.example.myauth.dto.dm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DmRoomListItemResponse {
  private Long roomId;
  private DmPeerUserResponse peerUser;
  private String lastMessagePreview;
  private LocalDateTime lastMessageAt;
  private Long unreadCount;
}
