package com.example.myauth.dto.dm;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DmReadRequest {

  @NotNull(message = "lastReadMessageId는 필수입니다.")
  private Long lastReadMessageId;
}
