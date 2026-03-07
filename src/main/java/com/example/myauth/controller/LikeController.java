package com.example.myauth.controller;

import com.example.myauth.dto.ApiResponse;
import com.example.myauth.dto.like.LikeResponse;
import com.example.myauth.dto.like.LikeUserResponse;
import com.example.myauth.entity.User;
import com.example.myauth.service.LikeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 좋아요 컨트롤러
 * 게시글/댓글 좋아요 API 엔드포인트 제공
 *
 * 【API 목록】
 * - POST   /api/posts/{postId}/like         : 게시글 좋아요
 * - DELETE /api/posts/{postId}/like         : 게시글 좋아요 취소
 * - GET    /api/posts/{postId}/likes        : 게시글 좋아요 누른 사용자 목록
 * - POST   /api/comments/{commentId}/like   : 댓글 좋아요
 * - DELETE /api/comments/{commentId}/like   : 댓글 좋아요 취소
 * - GET    /api/comments/{commentId}/likes  : 댓글 좋아요 누른 사용자 목록
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class LikeController {

  private final LikeService likeService;

  // ===== 게시글 좋아요 =====

  /**
   * 게시글 좋아요
   *
   * POST /api/posts/{postId}/like
   *
   * 【응답 예시】
   * {
   *   "success": true,
   *   "message": "좋아요 완료",
   *   "data": {
   *     "targetType": "POST",
   *     "targetId": 1,
   *     "liked": true,
   *     "likeCount": 43
   *   }
   * }
   */
  @PostMapping("/api/posts/{postId}/like")
  public ResponseEntity<ApiResponse<LikeResponse>> likePost(
      @AuthenticationPrincipal User user,
      @PathVariable Long postId
  ) {
    log.info("게시글 좋아요 토글 요청 - userId: {}, postId: {}", user.getId(), postId);

    LikeResponse response = likeService.togglePostLike(user.getId(), postId);
    String message = response.getLiked() ? "좋아요 완료" : "좋아요 취소 완료";

    return ResponseEntity.ok(ApiResponse.success(message, response));
  }

  /**
   * 게시글 좋아요 취소
   *
   * DELETE /api/posts/{postId}/like
   */
  @DeleteMapping("/api/posts/{postId}/like")
  public ResponseEntity<ApiResponse<LikeResponse>> unlikePost(
      @AuthenticationPrincipal User user,
      @PathVariable Long postId
  ) {
    log.info("게시글 좋아요 취소 요청 - userId: {}, postId: {}", user.getId(), postId);

    LikeResponse response = likeService.unlikePost(user.getId(), postId);

    return ResponseEntity.ok(ApiResponse.success("좋아요 취소 완료", response));
  }

  /**
   * 게시글 좋아요 누른 사용자 목록 조회
   *
   * GET /api/posts/{postId}/likes?page=0&size=20
   *
   * 【쿼리 파라미터】
   * - page: 페이지 번호 (0부터 시작, 기본값 0)
   * - size: 페이지 크기 (기본값 20, 최대 50)
   */
  @GetMapping("/api/posts/{postId}/likes")
  public ResponseEntity<ApiResponse<Page<LikeUserResponse>>> getPostLikeUsers(
      @PathVariable Long postId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size
  ) {
    log.info("게시글 좋아요 사용자 목록 조회 - postId: {}", postId);

    // 페이지 크기 제한
    if (size > 50) size = 50;

    Pageable pageable = PageRequest.of(page, size);
    Page<LikeUserResponse> users = likeService.getPostLikeUsers(postId, pageable);

    return ResponseEntity.ok(ApiResponse.success("좋아요 사용자 목록 조회 성공", users));
  }

  // ===== 댓글 좋아요 =====

  /**
   * 댓글 좋아요
   *
   * POST /api/comments/{commentId}/like
   */
  @PostMapping("/api/comments/{commentId}/like")
  public ResponseEntity<ApiResponse<LikeResponse>> likeComment(
      @AuthenticationPrincipal User user,
      @PathVariable Long commentId
  ) {
    log.info("댓글 좋아요 토글 요청 - userId: {}, commentId: {}", user.getId(), commentId);

    LikeResponse response = likeService.toggleCommentLike(user.getId(), commentId);
    String message = response.getLiked() ? "좋아요 완료" : "좋아요 취소 완료";

    return ResponseEntity.ok(ApiResponse.success(message, response));
  }

  /**
   * 댓글 좋아요 취소
   *
   * DELETE /api/comments/{commentId}/like
   */
  @DeleteMapping("/api/comments/{commentId}/like")
  public ResponseEntity<ApiResponse<LikeResponse>> unlikeComment(
      @AuthenticationPrincipal User user,
      @PathVariable Long commentId
  ) {
    log.info("댓글 좋아요 취소 요청 - userId: {}, commentId: {}", user.getId(), commentId);

    LikeResponse response = likeService.unlikeComment(user.getId(), commentId);

    return ResponseEntity.ok(ApiResponse.success("좋아요 취소 완료", response));
  }

  /**
   * 댓글 좋아요 누른 사용자 목록 조회
   *
   * GET /api/comments/{commentId}/likes?page=0&size=20
   */
  @GetMapping("/api/comments/{commentId}/likes")
  public ResponseEntity<ApiResponse<Page<LikeUserResponse>>> getCommentLikeUsers(
      @PathVariable Long commentId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size
  ) {
    log.info("댓글 좋아요 사용자 목록 조회 - commentId: {}", commentId);

    // 페이지 크기 제한
    if (size > 50) size = 50;

    Pageable pageable = PageRequest.of(page, size);
    Page<LikeUserResponse> users = likeService.getCommentLikeUsers(commentId, pageable);

    return ResponseEntity.ok(ApiResponse.success("좋아요 사용자 목록 조회 성공", users));
  }
}
