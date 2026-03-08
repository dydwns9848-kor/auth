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
public class DmReadResponse {
  private Long roomId;
  private Long lastReadMessageId;
  private LocalDateTime lastReadAt;
}
