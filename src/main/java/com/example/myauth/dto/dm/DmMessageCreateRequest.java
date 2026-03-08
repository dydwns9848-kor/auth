package com.example.myauth.dto.dm;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DmMessageCreateRequest {

  @NotBlank(message = "메시지 내용을 입력해주세요.")
  @Size(min = 1, max = 2000, message = "메시지는 1자 이상 2000자 이하로 입력해주세요.")
  private String content;
}
