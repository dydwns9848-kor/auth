package com.example.myauth.dto.dm;

import com.example.myauth.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DmPeerUserResponse {
  private Long id;
  private String name;
  private String profileImage;

  public static DmPeerUserResponse from(User user) {
    return DmPeerUserResponse.builder()
        .id(user.getId())
        .name(user.getName())
        .profileImage(user.getProfileImage())
        .build();
  }
}
