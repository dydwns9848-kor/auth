package com.example.myauth.service;

import com.example.myauth.dto.dm.*;
import com.example.myauth.entity.DmMessage;
import com.example.myauth.entity.DmRoom;
import com.example.myauth.entity.DmRoomRead;
import com.example.myauth.entity.User;
import com.example.myauth.exception.DmAccessDeniedException;
import com.example.myauth.exception.DmRoomNotFoundException;
import com.example.myauth.exception.UserNotFoundException;
import com.example.myauth.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DmService {

  private final DmRoomRepository dmRoomRepository;
  private final DmMessageRepository dmMessageRepository;
  private final DmRoomReadRepository dmRoomReadRepository;
  private final UserRepository userRepository;

  /**
   * 대화방 생성 또는 기존 대화방 조회.
   * 두 사용자 조합(user1_id, user2_id)으로 기존 방을 찾고, 없으면 생성한다.
   *
   * @param meId 로그인 사용자 ID
   * @param targetUserId DM 대상 사용자 ID
   * @return 대화방 정보(상대 사용자, 마지막 메시지 미리보기, unreadCount 포함)
   */
  @Transactional
  public DmRoomResponse createOrGetRoom(Long meId, Long targetUserId) {
    log.info("DM 방 생성/조회 요청 - meId: {}, targetUserId: {}", meId, targetUserId);

    if (meId.equals(targetUserId)) {
      throw new IllegalArgumentException("본인과는 DM 대화방을 생성할 수 없습니다.");
    }

    User me = userRepository.findById(meId)
        .orElseThrow(() -> new UserNotFoundException(meId));
    User target = userRepository.findById(targetUserId)
        .orElseThrow(() -> new UserNotFoundException(targetUserId));

    long user1Id = Math.min(meId, targetUserId);
    long user2Id = Math.max(meId, targetUserId);

    DmRoom room = dmRoomRepository.findByUser1IdAndUser2Id(user1Id, user2Id)
        .orElseGet(() -> dmRoomRepository.save(DmRoom.of(me, target)));

    String lastMessagePreview = getLastMessagePreview(room.getLastMessageId());
    long unreadCount = getUnreadCount(room.getId(), meId);

    return DmRoomResponse.builder()
        .roomId(room.getId())
        .peerUser(DmPeerUserResponse.from(getPeerUser(room, meId)))
        .lastMessagePreview(lastMessagePreview)
        .lastMessageAt(room.getLastMessageAt())
        .unreadCount(unreadCount)
        .build();
  }

  /**
   * 로그인 사용자의 DM 대화방 목록 조회.
   * lastMessageAt 기준 최신순(Nulls Last)으로 페이징하여 반환한다.
   *
   * @param meId 로그인 사용자 ID
   * @param pageable 페이지/크기 정보
   * @return 대화방 목록 페이지
   */
  @Transactional(readOnly = true)
  public Page<DmRoomListItemResponse> getMyRooms(Long meId, Pageable pageable) {
    log.info("내 DM 방 목록 조회 - meId: {}, page: {}", meId, pageable.getPageNumber());

    Page<DmRoom> rooms = dmRoomRepository.findMyRooms(meId, pageable);
    Map<Long, String> lastMessagePreviewMap = getLastMessagePreviewMap(rooms.getContent());

    return rooms.map(room -> DmRoomListItemResponse.builder()
        .roomId(room.getId())
        .peerUser(DmPeerUserResponse.from(getPeerUser(room, meId)))
        .lastMessagePreview(lastMessagePreviewMap.get(room.getId()))
        .lastMessageAt(room.getLastMessageAt())
        .unreadCount(getUnreadCount(room.getId(), meId))
        .build());
  }

  /**
   * 대화방 메시지 목록 조회(커서 기반).
   * beforeId가 없으면 최신 메시지부터, 있으면 beforeId 이전 메시지부터 조회한다.
   *
   * @param meId 로그인 사용자 ID
   * @param roomId 대화방 ID
   * @param beforeId 커서 기준 메시지 ID(옵션)
   * @param pageable 조회 건수 정보
   * @return 메시지 목록과 다음 페이지 존재 여부(hasNext)
   */
  @Transactional(readOnly = true)
  public DmMessageSliceResponse getMessages(Long meId, Long roomId, Long beforeId, Pageable pageable) {
    DmRoom room = getAccessibleRoom(meId, roomId);

    Slice<DmMessage> slice = beforeId == null
        ? dmMessageRepository.findByRoomIdOrderByIdDesc(room.getId(), pageable)
        : dmMessageRepository.findByRoomIdAndIdLessThanOrderByIdDesc(room.getId(), beforeId, pageable);

    List<DmMessageResponse> items = slice.getContent().stream()
        .map(DmMessageResponse::from)
        .toList();

    return DmMessageSliceResponse.builder()
        .items(items)
        .hasNext(slice.hasNext())
        .build();
  }

  /**
   * 메시지 전송.
   * 메시지를 저장하고 대화방의 lastMessageId/lastMessageAt을 최신 값으로 갱신한다.
   *
   * @param meId 로그인 사용자 ID(송신자)
   * @param roomId 대화방 ID
   * @param request 메시지 작성 요청 DTO
   * @return 저장된 메시지 정보
   */
  @Transactional
  public DmMessageResponse sendMessage(Long meId, Long roomId, DmMessageCreateRequest request) {
    DmRoom room = getAccessibleRoom(meId, roomId);

    User sender = userRepository.getReferenceById(meId);
    LocalDateTime now = LocalDateTime.now();

    DmMessage message = DmMessage.builder()
        .room(room)
        .sender(sender)
        .content(request.getContent())
        .build();
    DmMessage saved = dmMessageRepository.save(message);

    room.setLastMessageId(saved.getId());
    room.setLastMessageAt(now);
    dmRoomRepository.save(room);

    if (saved.getCreatedAt() == null) {
      saved.setCreatedAt(now);
    }

    return DmMessageResponse.from(saved);
  }

  /**
   * 읽음 처리(upsert).
   * 기존 lastReadMessageId보다 큰 값일 때만 읽음 상태를 갱신한다.
   *
   * @param meId 로그인 사용자 ID
   * @param roomId 대화방 ID
   * @param request 마지막으로 읽은 메시지 ID 요청 DTO
   * @return 읽음 처리 결과(마지막 읽은 메시지 ID, 읽은 시각)
   */
  @Transactional
  public DmReadResponse markAsRead(Long meId, Long roomId, DmReadRequest request) {
    DmRoom room = getAccessibleRoom(meId, roomId);

    DmMessage targetMessage = dmMessageRepository.findById(request.getLastReadMessageId())
        .orElseThrow(() -> new IllegalArgumentException("마지막 읽은 메시지를 찾을 수 없습니다."));
    if (!targetMessage.getRoom().getId().equals(roomId)) {
      throw new IllegalArgumentException("해당 메시지는 요청한 DM 방에 속하지 않습니다.");
    }

    DmRoomRead roomRead = dmRoomReadRepository.findByRoomIdAndUserId(roomId, meId)
        .orElseGet(() -> DmRoomRead.builder()
            .room(room)
            .user(userRepository.getReferenceById(meId))
            .build());

    Long currentLastReadMessageId = roomRead.getLastReadMessage() == null
        ? null
        : roomRead.getLastReadMessage().getId();

    if (currentLastReadMessageId == null || request.getLastReadMessageId() > currentLastReadMessageId) {
      roomRead.setLastReadMessage(targetMessage);
      roomRead.setLastReadAt(LocalDateTime.now());
      roomRead = dmRoomReadRepository.save(roomRead);
    }

    return DmReadResponse.builder()
        .roomId(roomId)
        .lastReadMessageId(roomRead.getLastReadMessage() != null ? roomRead.getLastReadMessage().getId() : null)
        .lastReadAt(roomRead.getLastReadAt())
        .build();
  }

  private DmRoom getAccessibleRoom(Long meId, Long roomId) {
    DmRoom room = dmRoomRepository.findByIdWithUsers(roomId)
        .orElseThrow(() -> new DmRoomNotFoundException(roomId));

    if (!isParticipant(room, meId)) {
      throw new DmAccessDeniedException();
    }
    return room;
  }

  private boolean isParticipant(DmRoom room, Long meId) {
    return room.getUser1().getId().equals(meId) || room.getUser2().getId().equals(meId);
  }

  private User getPeerUser(DmRoom room, Long meId) {
    return room.getUser1().getId().equals(meId) ? room.getUser2() : room.getUser1();
  }

  private long getUnreadCount(Long roomId, Long meId) {
    Optional<DmRoomRead> roomRead = dmRoomReadRepository.findByRoomIdAndUserId(roomId, meId);
    if (roomRead.isEmpty() || roomRead.get().getLastReadMessage() == null) {
      return dmMessageRepository.countByRoomIdAndSenderIdNot(roomId, meId);
    }
    return dmMessageRepository.countUnreadAfter(roomId, meId, roomRead.get().getLastReadMessage().getId());
  }

  private String getLastMessagePreview(Long lastMessageId) {
    if (lastMessageId == null) {
      return null;
    }
    return dmMessageRepository.findById(lastMessageId)
        .map(DmMessage::getContent)
        .map(this::preview)
        .orElse(null);
  }

  private Map<Long, String> getLastMessagePreviewMap(List<DmRoom> rooms) {
    List<Long> lastMessageIds = rooms.stream()
        .map(DmRoom::getLastMessageId)
        .filter(Objects::nonNull)
        .toList();

    if (lastMessageIds.isEmpty()) {
      return Collections.emptyMap();
    }

    Map<Long, DmMessage> messageMap = dmMessageRepository.findAllById(lastMessageIds).stream()
        .collect(Collectors.toMap(DmMessage::getId, Function.identity()));

    Map<Long, String> previewMap = new HashMap<>();
    for (DmRoom room : rooms) {
      if (room.getLastMessageId() == null) {
        previewMap.put(room.getId(), null);
        continue;
      }
      DmMessage message = messageMap.get(room.getLastMessageId());
      previewMap.put(room.getId(), message != null ? preview(message.getContent()) : null);
    }
    return previewMap;
  }

  private String preview(String content) {
    if (content == null) {
      return null;
    }
    return content.length() <= 100 ? content : content.substring(0, 100);
  }
}
