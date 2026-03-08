package com.example.myauth.dto.dm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DmMessageSliceResponse {
  private List<DmMessageResponse> items;
  private Boolean hasNext;
}
