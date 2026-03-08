package com.example.myauth.controller;

import com.example.myauth.dto.ApiResponse;
import com.example.myauth.dto.dm.*;
import com.example.myauth.entity.User;
import com.example.myauth.service.DmService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/dm")
@RequiredArgsConstructor
public class DmController {

  private final DmService dmService;

  /**
   * 대화방 생성 또는 기존 대화방 조회.
   * 대상 사용자와 이미 방이 있으면 기존 방을 반환하고, 없으면 새로 생성한다.
   *
   * @param user 로그인 사용자
   * @param request 대상 사용자 ID를 담은 요청 DTO
   * @return 생성/조회된 대화방 정보(ApiResponse + DmRoomResponse)
   */
  @PostMapping("/rooms")
  public ResponseEntity<ApiResponse<DmRoomResponse>> createOrGetRoom(
      @AuthenticationPrincipal User user,
      @Valid @RequestBody DmRoomCreateRequest request
  ) {
    DmRoomResponse response = dmService.createOrGetRoom(user.getId(), request.getTargetUserId());
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("DM 방 조회/생성 성공", response));
  }

  /**
   * 로그인 사용자의 DM 대화방 목록 조회.
   * lastMessageAt 기준으로 최신순 정렬되며 page/size 페이징을 지원한다.
   *
   * @param user 로그인 사용자
   * @param page 페이지 번호(0부터 시작)
   * @param size 페이지 크기(최대 50)
   * @return 대화방 목록 페이지(ApiResponse + Page&lt;DmRoomListItemResponse&gt;)
   */
  @GetMapping("/rooms")
  public ResponseEntity<ApiResponse<Page<DmRoomListItemResponse>>> getMyRooms(
      @AuthenticationPrincipal User user,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size
  ) {
    if (size > 50) size = 50;

    Pageable pageable = PageRequest.of(page, size);
    Page<DmRoomListItemResponse> response = dmService.getMyRooms(user.getId(), pageable);
    return ResponseEntity.ok(ApiResponse.success("내 DM 방 목록 조회 성공", response));
  }

  /**
   * 특정 대화방의 메시지 목록 조회.
   * beforeId 커서가 있으면 해당 ID 이전 메시지를 최신순으로 반환한다.
   *
   * @param user 로그인 사용자
   * @param roomId 조회할 대화방 ID
   * @param beforeId 커서 기준 메시지 ID(옵션)
   * @param size 조회 건수(최대 100)
   * @return 메시지 슬라이스 응답(ApiResponse + DmMessageSliceResponse)
   */
  @GetMapping("/rooms/{roomId}/messages")
  public ResponseEntity<ApiResponse<DmMessageSliceResponse>> getMessages(
      @AuthenticationPrincipal User user,
      @PathVariable Long roomId,
      @RequestParam(required = false) Long beforeId,
      @RequestParam(defaultValue = "30") int size
  ) {
    if (size > 100) size = 100;

    Pageable pageable = PageRequest.of(0, size);
    DmMessageSliceResponse response = dmService.getMessages(user.getId(), roomId, beforeId, pageable);
    return ResponseEntity.ok(ApiResponse.success("DM 메시지 목록 조회 성공", response));
  }

  /**
   * 대화방에 메시지 전송.
   *
   * @param user 로그인 사용자(송신자)
   * @param roomId 메시지를 전송할 대화방 ID
   * @param request 메시지 본문 요청 DTO
   * @return 전송된 메시지 정보(ApiResponse + DmMessageResponse)
   */
  @PostMapping("/rooms/{roomId}/messages")
  public ResponseEntity<ApiResponse<DmMessageResponse>> sendMessage(
      @AuthenticationPrincipal User user,
      @PathVariable Long roomId,
      @Valid @RequestBody DmMessageCreateRequest request
  ) {
    DmMessageResponse response = dmService.sendMessage(user.getId(), roomId, request);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("DM 메시지 전송 성공", response));
  }

  /**
   * 대화방 읽음 처리.
   * lastReadMessageId를 기준으로 사용자 읽음 상태를 갱신한다.
   *
   * @param user 로그인 사용자
   * @param roomId 읽음 처리할 대화방 ID
   * @param request 마지막으로 읽은 메시지 ID 요청 DTO
   * @return 읽음 처리 결과(ApiResponse + DmReadResponse)
   */
  @PostMapping("/rooms/{roomId}/read")
  public ResponseEntity<ApiResponse<DmReadResponse>> markAsRead(
      @AuthenticationPrincipal User user,
      @PathVariable Long roomId,
      @Valid @RequestBody DmReadRequest request
  ) {
    DmReadResponse response = dmService.markAsRead(user.getId(), roomId, request);
    return ResponseEntity.ok(ApiResponse.success("DM 읽음 처리 성공", response));
  }
}
